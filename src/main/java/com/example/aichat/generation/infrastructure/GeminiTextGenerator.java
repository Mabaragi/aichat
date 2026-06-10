package com.example.aichat.generation.infrastructure;

import com.example.aichat.generation.application.GenerationException;
import com.example.aichat.generation.application.GenerationRequest;
import com.example.aichat.generation.application.GenerationResult;
import com.example.aichat.generation.application.TextGenerator;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.GenerateContentResponseUsageMetadata;

import java.util.Objects;

public class GeminiTextGenerator implements TextGenerator {

    private static final String PROVIDER = "gemini";

    private final ProviderGenerationGateway gateway;

    public GeminiTextGenerator() {
        this(createClient(System.getenv("GEMINI_API_KEY")));
    }

    public GeminiTextGenerator(Client client) {
        Objects.requireNonNull(client, "client is required");
        this.gateway = (modelName, prompt) -> generate(client, modelName, prompt);
    }

    GeminiTextGenerator(ProviderGenerationGateway gateway) {
        this.gateway = Objects.requireNonNull(gateway, "gateway is required");
    }

    @Override
    public GenerationResult generate(GenerationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }

        try {
            ProviderGenerationResponse response = gateway.generate(request.modelName(), request.prompt());
            if (response == null || response.content() == null || response.content().isBlank()) {
                throw new GenerationException(
                        PROVIDER,
                        request.modelName(),
                        "Gemini returned an empty text response"
                );
            }

            String modelName = response.modelName() == null || response.modelName().isBlank()
                    ? request.modelName()
                    : response.modelName();
            return new GenerationResult(
                    response.content(),
                    modelName,
                    response.inputTokens(),
                    response.outputTokens()
            );
        } catch (GenerationException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new GenerationException(
                    PROVIDER,
                    request.modelName(),
                    "Gemini text generation failed",
                    exception
            );
        }
    }

    static Client createClient(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new GenerationException(PROVIDER, null, "GEMINI_API_KEY is required");
        }
        return Client.builder()
                .apiKey(apiKey.trim())
                .build();
    }

    private static ProviderGenerationResponse generate(Client client, String modelName, String prompt) {
        GenerateContentResponse response = client.models.generateContent(modelName, prompt, null);
        GenerateContentResponseUsageMetadata usage = response.usageMetadata().orElse(null);

        return new ProviderGenerationResponse(
                response.text(),
                response.modelVersion().orElse(modelName),
                usage == null ? null : usage.promptTokenCount().orElse(null),
                usage == null ? null : usage.candidatesTokenCount().orElse(null)
        );
    }
}
