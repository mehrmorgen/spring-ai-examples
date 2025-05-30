package com.example.agentic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;

/**
 * Configuration for SQLite database.
 */
@Configuration
public class DatabaseConfig {

    /**
     * Creates a SQLite DataSource for production use.
     * This bean is not active in test or e2e profiles.
     */
    @Bean
    @Profile({"!test", "!e2e"})
    public DataSource sqliteDataSource() {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:agent-memory.db");
        return dataSource;
    }

    // Note: e2eDataSource is now provided by TestConfig for the e2e profile
}
