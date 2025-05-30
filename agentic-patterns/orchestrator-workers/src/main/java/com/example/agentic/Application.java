
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

// ------------------------------------------------------------
// ORCHESTRATOR WORKERS
// ------------------------------------------------------------

@SpringBootApplication
public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ChatClient.Builder chatClientBuilder) {

        var chatClient = chatClientBuilder.build();
        var agent = new OrchestratorWorkers(chatClient);

        // Process a task
        var response = agent.process(
                "Generate both technical and user-friendly documentation for a REST API endpoint"
        );

        // Access results
        LOGGER.info("Analysis: " + response.analysis());
        LOGGER.info("Worker Outputs: {}", response.workerResponses());


        return args -> new OrchestratorWorkers(chatClient)
                .process("Write a product description for a new eco-friendly water bottle");
    }
}
