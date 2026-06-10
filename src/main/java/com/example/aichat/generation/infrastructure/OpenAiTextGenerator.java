package com.example.aichat.generation.infrastructure;

import com.example.aichat.generation.application.GenerationException;
import com.example.aichat.generation.application.GenerationRequest;
import com.example.aichat.generation.application.GenerationResult;
import com.example.aichat.generation.application.TextGenerator;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ResponsesModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseUsage;

import java.util.Objects;

public class OpenAiTextGenerator implements TextGenerator {

    private static final String PROVIDER = "openai";

    private final ProviderGenerationGateway gateway;

    public OpenAiTextGenerator() {
        this(createClient(System.getenv("OPENAI_API_KEY")));
    }

    public OpenAiTextGenerator(OpenAIClient client) {
        Objects.requireNonNull(client, "client is required");
        this.gateway = (modelName, prompt) -> generate(client, modelName, prompt);
    }

    OpenAiTextGenerator(ProviderGenerationGateway gateway) {
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
                        "OpenAI returned an empty text response"
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
                    "OpenAI text generation failed",
                    exception
            );
        }
    }

    static OpenAIClient createClient(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new GenerationException(PROVIDER, null, "OPENAI_API_KEY is required");
        }
        return OpenAIOkHttpClient.builder()
                .apiKey(apiKey.trim())
                .build();
    }

    private static ProviderGenerationResponse generate(
            OpenAIClient client,
            String modelName,
            String prompt
    ) {
        Response response = client.responses().create(
                ResponseCreateParams.builder()
                        .model(modelName)
                        .input(prompt)
                        .build()
        );
        ResponseUsage usage = response.usage().orElse(null);

        return new ProviderGenerationResponse(
                extractText(response),
                extractModelName(response.model(), modelName),
                usage == null ? null : Math.toIntExact(usage.inputTokens()),
                usage == null ? null : Math.toIntExact(usage.outputTokens())
        );
    }

    private static String extractText(Response response) {
        return response.output().stream()
                .flatMap(item -> item.message().stream())
                .flatMap(message -> message.content().stream())
                .flatMap(content -> content.outputText().stream())
                .map(outputText -> outputText.text().trim())
                .filter(text -> !text.isBlank())
                .reduce((left, right) -> left + System.lineSeparator() + right)
                .orElse(null);
    }

    private static String extractModelName(ResponsesModel model, String fallback) {
        if (model.isString()) {
            return model.asString();
        }
        if (model.isChat()) {
            return model.asChat().asString();
        }
        return fallback;
    }
}
