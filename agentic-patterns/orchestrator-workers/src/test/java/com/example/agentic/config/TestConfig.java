package com.example.agentic.config;

import com.example.agentic.memory.AgentMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

/**
 * Test configuration for E2E tests.
 * This configuration replaces the Application class for tests.
 */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {"com.example.agentic.memory"})
public class TestConfig {

    /**
     * Creates an in-memory database for E2E tests.
     * This allows tests to run without requiring an actual SQLite file.
     */
    @Bean
    @Primary
    public DataSource e2eDataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("test-db")
                .build();
    }

    @Bean
    public DataSource sqliteDataSource() {
        return e2eDataSource();
    }

    /**
     * Creates a JdbcTemplate for E2E tests.
     */
    @Bean
    @Primary
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * Creates a simple ChatModel for E2E tests.
     */
    @Bean
    @Primary
    public ChatModel chatModel() {
        return prompt -> null;
    }

    /**
     * Creates a ChatClient bean for E2E tests.
     */
    @Bean
    @Primary
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

    /**
     * Creates a mock AgentMemory bean for testing.
     * This is used as a fallback if E2EAgentMemory is not available.
     */
    @Bean
    @Profile("!test")
    public AgentMemory mockAgentMemory() {
        return new AgentMemory() {
            @Override
            public void saveAnalysis(String taskId, String analysis) {
                // Do nothing
            }

            @Override
            public void saveWorkerResponse(String taskId, String workerId, String response) {
                // Do nothing
            }

            @Override
            public String getAnalysis(String taskId) {
                return "Mock analysis for " + taskId;
            }

            @Override
            public java.util.List<String> getWorkerResponses(String taskId) {
                return java.util.Arrays.asList("Mock response 1", "Mock response 2");
            }

            @Override
            public void clear(String taskId) {
                // Do nothing
            }
        };
    }
}
