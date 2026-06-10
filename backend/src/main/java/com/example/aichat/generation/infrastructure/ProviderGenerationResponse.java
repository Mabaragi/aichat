package com.example.aichat.generation.infrastructure;

record ProviderGenerationResponse(
        String content,
        String modelName,
        Integer inputTokens,
        Integer outputTokens
) {
}
