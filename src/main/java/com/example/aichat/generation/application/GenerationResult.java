package com.example.aichat.generation.application;

public record GenerationResult(String content, String modelName, Integer inputTokens, Integer outputTokens) {

    public GenerationResult {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content is required");
        }
        if (modelName == null || modelName.isBlank()) {
            throw new IllegalArgumentException("modelName is required");
        }

        validateTokenUsage(inputTokens, "inputTokens");
        validateTokenUsage(outputTokens, "outputTokens");

        content = content.trim();
        modelName = modelName.trim();
    }

    private static void validateTokenUsage(Integer tokenUsage, String fieldName) {
        if (tokenUsage != null && tokenUsage < 0) {
            throw new IllegalArgumentException(fieldName + " must be at least 0");
        }
    }
}
