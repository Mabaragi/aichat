package com.example.aichat.generation.infrastructure;

@FunctionalInterface
interface ProviderGenerationGateway {

    ProviderGenerationResponse generate(String modelName, String prompt);
}
