package com.example.agentic;

import com.example.agentic.memory.AgentMemory;
import com.example.agentic.memory.MemoryAgent;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the MemoryAgent interface that combines OrchestratorWorkers functionality with AgentMemory.
 * This class uses composition instead of inheritance, delegating to OrchestratorWorkers
 * and an AgentMemory implementation.
 */
public class MemoryOrchestratorWorkers implements MemoryAgent {
    private static final String WORKER_ID_PREFIX = "worker-";
    private final AgentMemory memory;
    private final OrchestratorWorkers orchestrator;

    /**
     * Creates a new MemoryOrchestratorWorkers with default prompts and the specified memory implementation.
     *
     * @param chatClient  The ChatClient to use for LLM interactions
     * @param agentMemory The memory implementation to use
     */
    public MemoryOrchestratorWorkers(ChatClient chatClient, AgentMemory agentMemory) {
        this(chatClient, new OrchestratorWorkers(chatClient), agentMemory);
    }

    /**
     * Creates a new MemoryOrchestratorWorkers with custom prompts and the specified memory implementation.
     *
     * @param chatClient         The ChatClient to use for LLM interactions
     * @param orchestratorPrompt Custom prompt for the orchestrator LLM
     * @param workerPrompt       Custom prompt for the worker LLMs
     * @param agentMemory        The memory implementation to use
     */
    public MemoryOrchestratorWorkers(ChatClient chatClient, String orchestratorPrompt, String workerPrompt, AgentMemory agentMemory) {
        this(chatClient, new OrchestratorWorkers(chatClient, orchestratorPrompt, workerPrompt), agentMemory);
    }

    /**
     * Creates a new MemoryOrchestratorWorkers with a pre-configured orchestrator and memory.
     *
     * @param chatClient   The ChatClient to use for LLM interactions
     * @param orchestrator The OrchestratorWorkers instance to use
     * @param agentMemory  The memory implementation to use
     */
    public MemoryOrchestratorWorkers(ChatClient chatClient, OrchestratorWorkers orchestrator, AgentMemory agentMemory) {
        Assert.notNull(chatClient, "ChatClient must not be null");
        Assert.notNull(orchestrator, "OrchestratorWorkers must not be null");
        Assert.notNull(agentMemory, "AgentMemory must not be null");

        this.orchestrator = orchestrator;
        this.memory = agentMemory;
    }

    /**
     * Process a task using the orchestrator.
     *
     * @param taskDescription Description of the task to process
     * @return FinalResponse containing the analysis and worker outputs
     */
    public OrchestratorWorkers.FinalResponse process(String taskDescription) {
        return orchestrator.process(taskDescription);
    }

    private void saveWorkerResponses(String taskId, List<String> workerResponses) {
        for (var i = 0; i < workerResponses.size(); i++) {
            memory.saveWorkerResponse(taskId, WORKER_ID_PREFIX + i, workerResponses.get(i));
        }
    }

    @Override
    public void saveAnalysis(String taskId, String analysis) {
        memory.saveAnalysis(taskId, analysis);
    }

    @Override
    public void saveWorkerResponse(String taskId, String workerId, String response) {
        memory.saveWorkerResponse(taskId, workerId, response);
    }

    @Override
    public String getAnalysis(String taskId) {
        return memory.getAnalysis(taskId);
    }

    @Override
    public List<String> getWorkerResponses(String taskId) {
        return memory.getWorkerResponses(taskId);
    }

    @Override
    public void clear(String taskId) {
        memory.clear(taskId);
    }

    @Override
    public OrchestratorWorkers.FinalResponse processWithMemory(String taskId, String taskDescription) {
        var response = process(taskDescription);
        saveAnalysis(taskId, response.analysis());
        saveWorkerResponses(taskId, response.workerResponses());
        return response;
    }

    @Override
    public OrchestratorWorkers.FinalResponse getResponseFromMemory(String taskId) {
        return Optional.ofNullable(getAnalysis(taskId))
                .map(analysis -> new OrchestratorWorkers.FinalResponse(analysis, getWorkerResponses(taskId)))
                .orElse(null);
    }

    /**
     * Convenience method to generate a unique task ID and process a task.
     *
     * @param taskDescription Description of the task to be processed
     * @return FinalResponse containing the orchestrator's analysis and combined worker outputs
     */
    public OrchestratorWorkers.FinalResponse processWithMemory(String taskDescription) {
        var taskId = UUID.randomUUID().toString();
        return processWithMemory(taskId, taskDescription);
    }
}