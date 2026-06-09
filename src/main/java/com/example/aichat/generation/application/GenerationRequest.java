package com.example.aichat.generation.application;

public record GenerationRequest(String prompt, String modelName) {

    public GenerationRequest {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("prompt is required");
        }
        if (modelName == null || modelName.isBlank()) {
            throw new IllegalArgumentException("modelName is required");
        }

        prompt = prompt.trim();
        modelName = modelName.trim();
    }
}
