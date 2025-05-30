package com.example.agentic.memory;

import com.example.agentic.OrchestratorWorkers.FinalResponse;

/**
 * Interface that combines agent capabilities with memory functionality.
 * This allows agents to both process tasks and maintain memory of past interactions.
 */
public interface MemoryAgent extends AgentMemory {

    /**
     * Processes a task and stores the results in memory.
     *
     * @param taskId          The unique identifier for the task
     * @param taskDescription Description of the task to be processed
     * @return FinalResponse containing the orchestrator's analysis and combined worker outputs
     */
    FinalResponse processWithMemory(String taskId, String taskDescription);

    /**
     * Retrieves the complete response for a previously processed task from memory.
     *
     * @param taskId The unique identifier for the task
     * @return FinalResponse containing the orchestrator's analysis and combined worker outputs,
     * or null if the task hasn't been processed
     */
    FinalResponse getResponseFromMemory(String taskId);
}