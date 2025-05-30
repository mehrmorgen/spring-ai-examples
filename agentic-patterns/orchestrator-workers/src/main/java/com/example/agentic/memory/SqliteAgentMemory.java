package com.example.agentic.memory;

import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;

/**
 * SQLite implementation of AgentMemory for production use.
 */
@Component
@Profile("!test")
public class SqliteAgentMemory implements AgentMemory {
    private final JdbcTemplate jdbcTemplate;

    public SqliteAgentMemory(@org.springframework.beans.factory.annotation.Qualifier("sqliteDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        initializeDatabase();
    }

    private void initializeDatabase() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS analyses (task_id TEXT PRIMARY KEY, analysis TEXT)");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS worker_responses (task_id TEXT, worker_id TEXT, response TEXT, PRIMARY KEY (task_id, worker_id))");
    }

    @Override
    public void saveAnalysis(String taskId, String analysis) {
        // First delete any existing record
        jdbcTemplate.update("DELETE FROM analyses WHERE task_id = ?", taskId);
        // Then insert the new record
        jdbcTemplate.update("INSERT INTO analyses (task_id, analysis) VALUES (?, ?)", taskId, analysis);
    }

    @Override
    public void saveWorkerResponse(String taskId, String workerId, String response) {
        // First delete any existing record
        jdbcTemplate.update("DELETE FROM worker_responses WHERE task_id = ? AND worker_id = ?", taskId, workerId);
        // Then insert the new record
        jdbcTemplate.update("INSERT INTO worker_responses (task_id, worker_id, response) VALUES (?, ?, ?)",
                taskId, workerId, response);
    }

    @Override
    public String getAnalysis(String taskId) {
        try {
            return jdbcTemplate.queryForObject("SELECT analysis FROM analyses WHERE task_id = ?", String.class, taskId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<String> getWorkerResponses(String taskId) {
        return jdbcTemplate.queryForList("SELECT response FROM worker_responses WHERE task_id = ?", String.class, taskId);
    }

    @Override
    public void clear(String taskId) {
        jdbcTemplate.update("DELETE FROM analyses WHERE task_id = ?", taskId);
        jdbcTemplate.update("DELETE FROM worker_responses WHERE task_id = ?", taskId);
    }
}
