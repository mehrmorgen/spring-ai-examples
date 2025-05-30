package com.example.agentic.memory;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * SQLite implementation of AgentMemory specifically for E2E tests.
 * This implementation uses the e2e database configured in application-e2e.properties.
 */
@Component
@Profile("e2e")
public class E2EAgentMemory extends SqliteAgentMemory {

    /**
     * Creates a new E2EAgentMemory with the specified DataSource.
     *
     * @param dataSource The DataSource to use for database operations
     */
    public E2EAgentMemory(@org.springframework.beans.factory.annotation.Qualifier("e2eDataSource") DataSource dataSource) {
        super(dataSource);
    }
}