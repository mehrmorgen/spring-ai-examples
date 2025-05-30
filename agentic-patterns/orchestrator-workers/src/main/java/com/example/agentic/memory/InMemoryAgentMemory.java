package com.example.agentic.memory;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory implementation of AgentMemory for testing purposes.
 */
@Component
@Profile("test")
public class InMemoryAgentMemory implements AgentMemory {
    private final Map<String, String> analyses = new HashMap<>();
    private final Map<String, Map<String, String>> workerResponses = new HashMap<>();

    @Override
    public void saveAnalysis(String taskId, String analysis) {
        analyses.put(taskId, analysis);
    }

    @Override
    public void saveWorkerResponse(String taskId, String workerId, String response) {
        workerResponses.computeIfAbsent(taskId, k -> new HashMap<>()).put(workerId, response);
    }

    @Override
    public String getAnalysis(String taskId) {
        return analyses.get(taskId);
    }

    @Override
    public List<String> getWorkerResponses(String taskId) {
        Map<String, String> responses = workerResponses.getOrDefault(taskId, Collections.emptyMap());
        return new ArrayList<>(responses.values());
    }

    @Override
    public void clear(String taskId) {
        analyses.remove(taskId);
        workerResponses.remove(taskId);
    }
}