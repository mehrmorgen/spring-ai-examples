package com.example.agentic;

import com.example.agentic.config.TestConfig;
import com.example.agentic.memory.AgentMemory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for the Application class.
 * These tests verify that the Spring application context loads correctly
 * and that the required beans are available.
 */
@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
class ApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private AgentMemory inMemoryAgentMemory;

    @Test
    void contextLoads() {
        // Verify that the application context loads successfully
        assertNotNull(context);
    }

    @Test
    void inMemoryAgentMemoryBeanIsAvailable() {
        // Verify that the inMemoryAgentMemory bean is available
        assertNotNull(inMemoryAgentMemory);

        // Test the inMemoryAgentMemory methods
        var taskId = "test-task";

        // Save some data
        inMemoryAgentMemory.saveAnalysis(taskId, "Test analysis");
        inMemoryAgentMemory.saveWorkerResponse(taskId, "worker-1", "Test response");

        // Retrieve and verify
        var analysis = inMemoryAgentMemory.getAnalysis(taskId);
        assertNotNull(analysis);

        // Test worker responses
        var responses = inMemoryAgentMemory.getWorkerResponses(taskId);
        assertNotNull(responses);

        // Test clearing
        inMemoryAgentMemory.clear(taskId);
    }
}
