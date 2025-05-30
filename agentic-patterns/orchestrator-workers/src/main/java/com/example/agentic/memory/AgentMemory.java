package com.example.agentic.memory;

import java.util.List;

/**
 * Interface for agent memory storage that can be implemented by different backends.
 */
public interface AgentMemory {
    /**
     * Saves the orchestrator's analysis for a task
     */
    void saveAnalysis(String taskId, String analysis);

    /**
     * Saves a worker's response for a task
     */
    void saveWorkerResponse(String taskId, String workerId, String response);

    /**
     * Retrieves the orchestrator's analysis for a task
     */
    String getAnalysis(String taskId);

    /**
     * Retrieves all worker responses for a task
     */
    List<String> getWorkerResponses(String taskId);

    /**
     * Clears all data for a task
     */
    void clear(String taskId);
}