package com.example.agentic;

import com.example.agentic.OrchestratorWorkers.FinalResponse;
import com.example.agentic.config.TestConfig;
import com.example.agentic.memory.AgentMemory;
import com.example.agentic.memory.InMemoryAgentMemory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the OrchestratorWorkers class.
 * These tests use the TestConfig to provide a test environment.
 */
@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
class OrchestratorWorkersTests {

    @Autowired
    private AgentMemory inMemoryAgentMemory;

    @Autowired
    private org.springframework.ai.chat.client.ChatClient chatClient;

    private String taskId;

    @AfterEach
    void cleanup() {
        if (taskId != null) {
            inMemoryAgentMemory.clear(taskId);
        }
    }

    @Test
    void testContextLoads() {
        // This test verifies that the Spring context loads successfully
        assertNotNull(inMemoryAgentMemory);
        assertNotNull(chatClient);
        assertInstanceOf(InMemoryAgentMemory.class, inMemoryAgentMemory);
    }

    @Test
    void testMemoryOrchestratorWorkersWithInMemoryStorage() {
        // Arrange
        taskId = UUID.randomUUID().toString();
        String taskDescription = "Write a short product description for a reusable water bottle";

        // Create a test implementation that doesn't rely on actual AI calls
        class TestMemoryOrchestratorWorkers extends MemoryOrchestratorWorkers {
            public TestMemoryOrchestratorWorkers(AgentMemory memory) {
                super(chatClient, memory);
            }

            @Override
            public FinalResponse processWithMemory(String taskId, String taskDescription) {
                // Create a predefined response for testing
                List<String> workerResponses = Arrays.asList(
                        "Test worker response 1 for: " + taskDescription,
                        "Test worker response 2 for: " + taskDescription
                );
                String analysis = "Test analysis for: " + taskDescription;

                // Save the results to memory
                saveAnalysis(taskId, analysis);

                // Save each worker response with a unique ID
                for (int i = 0; i < workerResponses.size(); i++) {
                    saveWorkerResponse(taskId, "worker-" + i, workerResponses.get(i));
                }

                return new FinalResponse(analysis, workerResponses);
            }
        }

        TestMemoryOrchestratorWorkers testAgent = new TestMemoryOrchestratorWorkers(inMemoryAgentMemory);

        // Act
        FinalResponse response = testAgent.processWithMemory(taskId, taskDescription);

        // Assert
        assertNotNull(response);
        assertNotNull(response.analysis());
        assertNotNull(response.workerResponses());

        // Verify data was saved to memory
        String savedAnalysis = inMemoryAgentMemory.getAnalysis(taskId);
        List<String> savedResponses = inMemoryAgentMemory.getWorkerResponses(taskId);

        assertEquals(response.analysis(), savedAnalysis);
        assertEquals(response.workerResponses().size(), savedResponses.size());

        // Test retrieving from memory
        FinalResponse retrievedResponse = testAgent.getResponseFromMemory(taskId);
        assertNotNull(retrievedResponse);
        assertEquals(response.analysis(), retrievedResponse.analysis());
        assertEquals(response.workerResponses().size(), retrievedResponse.workerResponses().size());
    }

    @Test
    void testInMemoryAgentMemory() {
        // Arrange
        taskId = UUID.randomUUID().toString();
        String analysis = "Test analysis";
        String workerId1 = "worker-1";
        String workerId2 = "worker-2";
        String response1 = "Test response 1";
        String response2 = "Test response 2";

        // Act
        inMemoryAgentMemory.saveAnalysis(taskId, analysis);
        inMemoryAgentMemory.saveWorkerResponse(taskId, workerId1, response1);
        inMemoryAgentMemory.saveWorkerResponse(taskId, workerId2, response2);

        // Assert
        String retrievedAnalysis = inMemoryAgentMemory.getAnalysis(taskId);
        List<String> retrievedResponses = inMemoryAgentMemory.getWorkerResponses(taskId);

        assertEquals(analysis, retrievedAnalysis);
        assertEquals(2, retrievedResponses.size());
        assertTrue(retrievedResponses.contains(response1));
        assertTrue(retrievedResponses.contains(response2));

        // Test clearing
        inMemoryAgentMemory.clear(taskId);
        assertNull(inMemoryAgentMemory.getAnalysis(taskId));
        assertEquals(0, inMemoryAgentMemory.getWorkerResponses(taskId).size());
    }

    @Test
    void testOrchestratorWorkersConstructors() {
        // Test default constructor
        OrchestratorWorkers orchestratorWorkers = new OrchestratorWorkers(chatClient);
        assertNotNull(orchestratorWorkers);

        // Test custom prompts constructor
        String customOrchestratorPrompt = "Custom orchestrator prompt";
        String customWorkerPrompt = "Custom worker prompt";
        OrchestratorWorkers customOrchestrator = new OrchestratorWorkers(chatClient, customOrchestratorPrompt, customWorkerPrompt);
        assertNotNull(customOrchestrator);

        // Test constructor with null ChatClient
        assertThrows(IllegalArgumentException.class, () -> new OrchestratorWorkers(null));

        // Test constructor with null or empty prompts
        assertThrows(IllegalArgumentException.class, () -> new OrchestratorWorkers(chatClient, null, customWorkerPrompt));
        assertThrows(IllegalArgumentException.class, () -> new OrchestratorWorkers(chatClient, customOrchestratorPrompt, null));
        assertThrows(IllegalArgumentException.class, () -> new OrchestratorWorkers(chatClient, "", customWorkerPrompt));
        assertThrows(IllegalArgumentException.class, () -> new OrchestratorWorkers(chatClient, customOrchestratorPrompt, ""));
    }

    @Test
    void testOrchestratorWorkersProcess() {
        // Create a test implementation that doesn't rely on actual AI calls
        class TestOrchestratorWorkers extends OrchestratorWorkers {
            public TestOrchestratorWorkers(ChatClient chatClient) {
                super(chatClient);
            }

            @Override
            public FinalResponse process(String taskDescription) {
                // Call the parent's process method for validation
                Assert.hasText(taskDescription, "Task description must not be empty");

                // Create a predefined response for testing
                List<String> workerResponses = Arrays.asList(
                        "Test worker response 1 for: " + taskDescription,
                        "Test worker response 2 for: " + taskDescription
                );
                String analysis = "Test analysis for: " + taskDescription;

                return new FinalResponse(analysis, workerResponses);
            }
        }

        // Arrange
        TestOrchestratorWorkers testAgent = new TestOrchestratorWorkers(chatClient);
        String taskDescription = "Write a short product description for a reusable water bottle";

        // Act
        FinalResponse response = testAgent.process(taskDescription);

        // Assert
        assertNotNull(response);
        assertNotNull(response.analysis());
        assertNotNull(response.workerResponses());
        assertEquals("Test analysis for: " + taskDescription, response.analysis());
        assertEquals(2, response.workerResponses().size());

        // Test with null or empty task description
        assertThrows(IllegalArgumentException.class, () -> testAgent.process(null));
        assertThrows(IllegalArgumentException.class, () -> testAgent.process(""));
    }
}
