package com.example.agentic;

import com.example.agentic.OrchestratorWorkers.FinalResponse;
import com.example.agentic.config.TestConfig;
import com.example.agentic.memory.AgentMemory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * End-to-end tests for MemoryOrchestratorWorkers using SQLite storage.
 * These tests verify that the memory functionality works correctly with a real SQLite database.
 * <p>
 * Note: We're using TestConfig as the main configuration class for the tests,
 * which provides all necessary beans without loading the Application class.
 */
@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("e2e")
class MemoryOrchestratorWorkersE2ETest {

    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("e2EAgentMemory")
    private AgentMemory memory;

    @Autowired
    private org.springframework.ai.chat.client.ChatClient chatClient;

    private String taskId;

    @AfterEach
    void cleanup() {
        if (taskId != null) {
            memory.clear(taskId);
        }
    }

    @Test
    void testProcessWithMemory() {
        // Arrange
        taskId = UUID.randomUUID().toString();
        TestMemoryOrchestratorWorkers agent = new TestMemoryOrchestratorWorkers(memory);
        String taskDescription = "Write a short product description for a reusable water bottle";

        // Act
        FinalResponse response = agent.processWithMemory(taskId, taskDescription);

        // Assert
        assertNotNull(response);
        assertNotNull(response.analysis());
        assertNotNull(response.workerResponses());

        // Verify data was saved to SQLite
        String savedAnalysis = memory.getAnalysis(taskId);
        List<String> savedResponses = memory.getWorkerResponses(taskId);

        assertEquals(response.analysis(), savedAnalysis);
        assertEquals(response.workerResponses().size(), savedResponses.size());
    }

    @Test
    void testGetResponseFromMemory() {
        // Arrange
        taskId = UUID.randomUUID().toString();
        TestMemoryOrchestratorWorkers agent = new TestMemoryOrchestratorWorkers(memory);
        String taskDescription = "Write a short product description for a reusable water bottle";

        // Act - First process and save to memory
        FinalResponse originalResponse = agent.processWithMemory(taskId, taskDescription);

        // Then retrieve from memory
        FinalResponse retrievedResponse = agent.getResponseFromMemory(taskId);

        // Assert
        assertNotNull(retrievedResponse);
        assertEquals(originalResponse.analysis(), retrievedResponse.analysis());
        assertEquals(originalResponse.workerResponses().size(), retrievedResponse.workerResponses().size());
    }

    @Test
    void testMultipleTasksWithMemory() {
        // Arrange
        TestMemoryOrchestratorWorkers agent = new TestMemoryOrchestratorWorkers(memory);

        // Create first task
        String taskId1 = UUID.randomUUID().toString();
        String taskDescription1 = "Write a short product description for a reusable water bottle";

        // Create second task
        String taskId2 = UUID.randomUUID().toString();
        String taskDescription2 = "Write a short product description for a smart watch";

        // Act
        FinalResponse response1 = agent.processWithMemory(taskId1, taskDescription1);
        FinalResponse response2 = agent.processWithMemory(taskId2, taskDescription2);

        // Retrieve from memory
        FinalResponse retrieved1 = agent.getResponseFromMemory(taskId1);
        FinalResponse retrieved2 = agent.getResponseFromMemory(taskId2);

        // Assert
        assertNotNull(retrieved1);
        assertNotNull(retrieved2);
        assertEquals(response1.analysis(), retrieved1.analysis());
        assertEquals(response2.analysis(), retrieved2.analysis());

        // Cleanup
        memory.clear(taskId1);
        memory.clear(taskId2);
    }

    /**
     * Test implementation of MemoryOrchestratorWorkers that doesn't rely on actual AI calls.
     * This class overrides the process method to return predefined responses.
     */
    private class TestMemoryOrchestratorWorkers extends MemoryOrchestratorWorkers {
        public TestMemoryOrchestratorWorkers(AgentMemory memory) {
            // Use the autowired ChatClient
            super(chatClient, memory);
        }

        @Override
        public FinalResponse process(String taskDescription) {
            // Return a predefined response for testing
            List<String> workerResponses = Arrays.asList(
                    "Test worker response 1 for: " + taskDescription,
                    "Test worker response 2 for: " + taskDescription
            );
            return new FinalResponse("Test analysis for: " + taskDescription, workerResponses);
        }

        @Override
        public FinalResponse processWithMemory(String taskId, String taskDescription) {
            // Process the task using our overridden process method
            FinalResponse response = process(taskDescription);

            // Save the results to memory
            saveAnalysis(taskId, response.analysis());

            // Save each worker response with a unique ID
            List<String> workerResponses = response.workerResponses();
            for (int i = 0; i < workerResponses.size(); i++) {
                saveWorkerResponse(taskId, "worker-" + i, workerResponses.get(i));
            }

            return response;
        }
    }
}
