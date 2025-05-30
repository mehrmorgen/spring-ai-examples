
# Rich Agent Orchestration with Memory and Tool Use

Based on the Spring AI Tutorial, original README

## Code Style and Best Practices Guide

This document provides guidance for generating code and content that adheres to the established patterns and practices in the Orchestrator-Workers project. Following these guidelines will ensure consistency and maintainability.

## 1. Java Code Style

### Class Structure
- **Use Records for Data Transfer Objects**: Use Java records for immutable data transfer objects (DTOs) like `Task`, `OrchestratorResponse`, and `FinalResponse`.
  - **Pros**: Concise syntax, built-in equals/hashCode/toString, immutability
  - **Cons**: Limited to data carrier use cases, cannot extend other classes
  - **Example**:
  ```java
  public static record OrchestratorResponse(String analysis, List<Task> tasks) {}
  ```

- **Interface-based Design**: Define interfaces before implementations to enable multiple implementations and facilitate testing.
  - **Pros**: Promotes loose coupling, enables dependency injection, simplifies testing
  - **Cons**: Can add complexity for simple use cases
  - **Example**: `AgentMemory` interface with `InMemoryAgentMemory` and `SqliteAgentMemory` implementations

### Documentation
- **Comprehensive JavaDoc**: Include detailed JavaDoc for all public classes, methods, and interfaces.
  - **Pros**: Self-documenting code, IDE assistance, better developer experience
  - **Cons**: Requires maintenance when code changes
  - **Example**:
  ```java
  /**
   * Processes a task using the orchestrator-workers pattern.
   * First, the orchestrator analyzes the task and breaks it down into subtasks.
   * Then, workers execute each subtask in parallel.
   * Finally, the results are combined into a single response.
   * 
   * @param taskDescription Description of the task to be processed
   * @return WorkerResponse containing the orchestrator's analysis and combined worker outputs
   * @throws IllegalArgumentException if taskDescription is null or empty
   */
  ```

### Error Handling
- **Precondition Validation**: Use Spring's `Assert` utility for validating method parameters.
  - **Pros**: Clear error messages, fail-fast approach, consistent validation
  - **Cons**: Adds verbosity to method beginnings
  - **Example**:
  ```java
  Assert.notNull(chatClient, "ChatClient must not be null");
  Assert.hasText(taskDescription, "Task description must not be empty");
  ```

- **Exception Handling**: Use try-catch blocks with specific exception types and provide meaningful error messages.
  - **Example**:
  ```java
  try {
      return jdbcTemplate.queryForObject("SELECT analysis FROM analyses WHERE task_id = ?", String.class, taskId);
  } catch (EmptyResultDataAccessException e) {
      return null;
  }
  ```

## 2. Project Structure

### Package Organization
- **Feature-based Packaging**: Organize code by feature rather than by layer.
  - **Pros**: Related code stays together, easier to understand feature boundaries
  - **Cons**: May lead to some duplication across features
  - **Example**: Memory-related classes are in the `com.example.agentic.memory` package

### Project Directory Structure
```
orchestrator-workers/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── agentic/
│   │   │               ├── memory/                 # Memory-related components
│   │   │               │   ├── AgentMemory.java    # Memory interface
│   │   │               │   ├── InMemoryAgentMemory.java  # Test implementation
│   │   │               │   ├── MemoryAgent.java    # Memory-enabled agent interface
│   │   │               │   └── SqliteAgentMemory.java    # Production implementation
│   │   │               ├── Application.java        # Main application entry point
│   │   │               ├── MemoryOrchestratorWorkers.java  # Memory-enabled implementation
│   │   │               └── OrchestratorWorkers.java  # Core pattern implementation
│   │   └── resources/
│   │       └── application.properties  # Application configuration
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── example/
│       │           └── agentic/
│       │               ├── config/                 # Test configuration
│       │               │   └── TestConfig.java     # Test-specific beans
│       │               ├── memory/                 # Memory test components
│       │               │   └── E2EAgentMemory.java # End-to-end test memory
│       │               └── OrchestratorWorkersTests.java  # Core tests
│       └── resources/
│           └── application-e2e.properties  # E2E test configuration
└── pom.xml                                 # Project dependencies
```

### Configuration
- **Profile-based Configuration**: Use Spring profiles to configure different environments.
  - **Pros**: Environment-specific behavior without code changes
  - **Cons**: Can make application startup logic more complex
  - **Example**:
  ```java
  @Component
  @Profile("!test")
  public class SqliteAgentMemory implements AgentMemory {
      // Implementation
  }
  ```

## 3. Prompt Engineering

### Prompt Templates
- **Use Multi-line String Literals**: Define prompt templates using Java text blocks (triple quotes).
  - **Pros**: Preserves formatting, improves readability
  - **Cons**: Only available in Java 15+
  - **Example**:
  ```java
  public static final String DEFAULT_ORCHESTRATOR_PROMPT = """
          Analyze this task and break it down into 2-3 distinct approaches:
          
          Task: {task}
          
          Return your response in this JSON format:
          \\{
          "analysis": "Explain your understanding of the task and which variations would be valuable.
                       Focus on how each approach serves different aspects of the task.",
          "tasks": [
              \\{
              "type": "formal",
              "description": "Write a precise, technical version that emphasizes specifications"
              \\},
              \\{
              "type": "conversational",
              "description": "Write an engaging, friendly version that connects with readers"
              \\}
          ]
          \\}
          """;
  ```

- **Parameterized Prompts**: Use placeholders in prompts that can be replaced at runtime.
  - **Pros**: Reusable templates, consistent prompt structure
  - **Cons**: Need to ensure all parameters are provided
  - **Example**: `{task}`, `{original_task}`, `{task_type}`, `{task_description}`

### Response Parsing
- **Structured Responses**: Request responses in structured formats (like JSON) for easier parsing.
  - **Pros**: Reliable extraction of information, consistent structure
  - **Cons**: More complex prompts, may limit model creativity
  - **Example**: Using Spring AI's entity mapping to parse JSON responses into Java objects:
  ```java
  OrchestratorResponse orchestratorResponse = this.chatClient.prompt()
          .user(u -> u.text(this.orchestratorPrompt)
                  .param("task", taskDescription))
          .call()
          .entity(OrchestratorResponse.class);
  ```

## 4. Memory Management

### Memory Interfaces
- **Consistent Memory API**: Use a consistent interface for memory operations.
  - **Pros**: Swappable implementations, consistent usage patterns
  - **Cons**: May not capture all specialized needs
  - **Example**: `AgentMemory` interface with methods like `saveAnalysis`, `getWorkerResponses`

### Storage Options
- **Multiple Storage Backends**: Support different storage options through interface implementations.
  - **Pros**: Flexibility for different use cases, environment-specific storage
  - **Cons**: Need to test all implementations
  - **Example**: `InMemoryAgentMemory` for testing, `SqliteAgentMemory` for production

## 5. Testing Practices

### Test-Driven Development (TDD)
- **Write Tests First**: Follow the TDD approach by writing tests before implementing functionality.
  - **Pros**: Ensures testable code, clear requirements, better design
  - **Cons**: Initial learning curve, may slow down initial development
  - **Example**:
  ```java
  @Test
  void shouldBreakTaskIntoSubtasks() {
      // Given
      String taskDescription = "Generate documentation for API";
      
      // When
      FinalResponse response = orchestratorWorkers.process(taskDescription);
      
      // Then
      assertThat(response.analysis()).isNotEmpty();
      assertThat(response.workerResponses()).hasSizeGreaterThan(1);
  }
  ```

- **Red-Green-Refactor Cycle**: Follow the TDD cycle:
  1. Write a failing test (Red)
  2. Implement just enough code to make the test pass (Green)
  3. Refactor the code while keeping tests passing
  - **Rationale**: Ensures code is always testable and meets requirements

### Test Profiles
- **Use Spring Profiles for Testing**: Configure test-specific beans using Spring profiles.
  - **Pros**: Isolates test environment, prevents production code from running in tests
  - **Cons**: Need to maintain separate configurations
  - **Example**: `@Profile("test")` for test-specific implementations

### Test Fixtures
- **Reusable Test Data**: Create test fixtures for common test data and scenarios.
  - **Pros**: Consistent test data, DRY principle, easier test maintenance
  - **Cons**: Can become complex if overused
  - **Example**:
  ```java
  public class TestFixtures {
      public static final String SAMPLE_TASK = "Generate documentation for REST API";
      
      public static OrchestratorResponse createSampleOrchestratorResponse() {
          return new OrchestratorResponse(
              "Sample analysis",
              List.of(
                  new Task("formal", "Write technical documentation"),
                  new Task("conversational", "Write user-friendly guide")
              )
          );
      }
      
      public static ChatClient createMockChatClient() {
          // Create and configure a mock ChatClient for testing
      }
  }
  ```

- **Test Data Builders**: Use the builder pattern for complex test data.
  - **Pros**: Flexible test data creation, readable test setup
  - **Cons**: Additional code to maintain
  - **Example**:
  ```java
  public class TaskBuilder {
      private String type = "default";
      private String description = "Default description";
      
      public TaskBuilder withType(String type) {
          this.type = type;
          return this;
      }
      
      public TaskBuilder withDescription(String description) {
          this.description = description;
          return this;
      }
      
      public Task build() {
          return new Task(type, description);
      }
      
      public static TaskBuilder aTask() {
          return new TaskBuilder();
      }
  }
  
  // Usage in tests
  Task task = TaskBuilder.aTask()
      .withType("formal")
      .withDescription("Write API docs")
      .build();
  ```

### End-to-End Testing
- **E2E Test Support**: Include support for end-to-end testing with specialized configurations.
  - **Pros**: Validates complete workflows, catches integration issues
  - **Cons**: Slower tests, more complex setup
  - **Example**: `E2EAgentMemory` class for end-to-end testing

## 6. Rationale and Trade-offs

### Why These Patterns?

1. **Interface-First Design**
   - **Rationale**: Enables multiple implementations and facilitates testing
   - **Trade-offs**: Adds complexity but improves flexibility and testability

2. **Immutable Data Objects**
   - **Rationale**: Prevents unexpected state changes, thread-safe
   - **Trade-offs**: Requires creating new objects for changes, but improves reliability

3. **Structured Prompts and Responses**
   - **Rationale**: Ensures consistent interaction with LLMs
   - **Trade-offs**: More complex prompts but more reliable parsing

4. **Memory Abstraction**
   - **Rationale**: Allows different storage strategies without changing agent logic
   - **Trade-offs**: Additional abstraction layer but enables flexibility in deployment

5. **Test-Driven Development**
   - **Rationale**: Ensures code quality and testability from the start
   - **Trade-offs**: Initial development may be slower, but maintenance is easier

6. **Test Fixtures**
   - **Rationale**: Provides consistent, reusable test data
   - **Trade-offs**: Requires maintenance but improves test readability and reliability

By following these guidelines, you'll ensure that generated code and content aligns with the established patterns and practices in the Orchestrator-Workers project, maintaining consistency and quality across the codebase.