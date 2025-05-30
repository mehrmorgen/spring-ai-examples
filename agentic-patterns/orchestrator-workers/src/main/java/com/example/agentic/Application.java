
/*
 * Copyright 2024 - 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.agentic;

import com.example.agentic.memory.AgentMemory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.List;

// ------------------------------------------------------------
// ORCHESTRATOR WORKERS
// ------------------------------------------------------------

@SpringBootApplication
public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /**
     * Creates a mock AgentMemory bean for demonstration purposes.
     * In a real application, this would be replaced by SqliteAgentMemory or InMemoryAgentMemory.
     */
    @Bean
    @org.springframework.context.annotation.Profile({"!test", "!e2e"})
    public AgentMemory mockAgentMemory() {
        return new AgentMemory() {
            @Override
            public void saveAnalysis(String taskId, String analysis) {
                LOGGER.info("Saving analysis for task {}: {}", taskId, analysis);
            }

            @Override
            public void saveWorkerResponse(String taskId, String workerId, String response) {
                LOGGER.info("Saving worker response for task {}, worker {}: {}", taskId, workerId, response);
            }

            @Override
            public String getAnalysis(String taskId) {
                LOGGER.info("Getting analysis for task {}", taskId);
                return "Mock analysis for task " + taskId;
            }

            @Override
            public List<String> getWorkerResponses(String taskId) {
                LOGGER.info("Getting worker responses for task {}", taskId);
                return Arrays.asList("Mock worker response 1", "Mock worker response 2");
            }

            @Override
            public void clear(String taskId) {
                LOGGER.info("Clearing data for task {}", taskId);
            }
        };
    }

    @Bean
    @org.springframework.context.annotation.Profile({"!test", "!e2e"})
    public CommandLineRunner commandLineRunner(ChatClient.Builder chatClientBuilder, @Qualifier("mockAgentMemory") AgentMemory memory) {

        var chatClient = chatClientBuilder.build();

        // Example 1: Using OrchestratorWorkers without memory
        LOGGER.info("Example 1: Using OrchestratorWorkers without memory");
        var agent = new OrchestratorWorkers(chatClient);
        var response = agent.process(
                "Generate both technical and user-friendly documentation for a REST API endpoint"
        );
        LOGGER.info("Analysis: {}", response.analysis());
        LOGGER.info("Worker Outputs: {}", response.workerResponses());

        // Example 2: Using MemoryOrchestratorWorkers with memory
        LOGGER.info("Example 2: Using MemoryOrchestratorWorkers with memory");
        var memoryAgent = new MemoryOrchestratorWorkers(chatClient, memory);
        var taskId = "eco-bottle-task";
        var memoryResponse = memoryAgent.processWithMemory(
                taskId, "Write a product description for a new eco-friendly water bottle"
        );
        LOGGER.info("Analysis: {}", memoryResponse.analysis());
        LOGGER.info("Worker Outputs: {}", memoryResponse.workerResponses());

        // Example 3: Retrieving from memory
        LOGGER.info("Example 3: Retrieving from memory");
        var retrievedResponse = memoryAgent.getResponseFromMemory(taskId);
        LOGGER.info("Retrieved Analysis: {}", retrievedResponse.analysis());
        LOGGER.info("Retrieved Worker Outputs: {}", retrievedResponse.workerResponses());

        return args -> {
            // This is just a placeholder for the CommandLineRunner
        };
    }
}
