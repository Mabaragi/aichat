package com.example.aichat.generation.infrastructure;

import com.example.aichat.debate.domain.ParticipantModel;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Locale;

@ConfigurationProperties(prefix = "generation")
public record GenerationProperties(
        Provider provider,
        Mock mock,
        OpenAi openai,
        Gemini gemini
) {

    public String resolveModelName(ParticipantModel participantModel) {
        if (participantModel == null) {
            throw new IllegalArgumentException("participantModel is required");
        }

        Provider activeProvider = provider == null ? Provider.MOCK : provider;
        return switch (participantModel) {
            case MOCK -> mockModelName();
            case FAST -> switch (activeProvider) {
                case OPENAI -> openaiModelName(ProviderModelSelector.FAST);
                case GEMINI -> geminiModelName(ProviderModelSelector.FAST);
                case MOCK -> mockModelName();
            };
            case BALANCED -> switch (activeProvider) {
                case OPENAI -> openaiModelName(ProviderModelSelector.BALANCED);
                case GEMINI -> geminiModelName(ProviderModelSelector.BALANCED);
                case MOCK -> mockModelName();
            };
            case QUALITY -> switch (activeProvider) {
                case OPENAI -> openaiModelName(ProviderModelSelector.QUALITY);
                case GEMINI -> geminiModelName(ProviderModelSelector.QUALITY);
                case MOCK -> mockModelName();
            };
        };
    }

    public String resolveActiveProviderName() {
        Provider activeProvider = provider == null ? Provider.MOCK : provider;
        return activeProvider.name().toLowerCase(Locale.ROOT);
    }

    public String openaiApiKey() {
        return openai == null ? null : openai.apiKey();
    }

    public String geminiApiKey() {
        return gemini == null ? null : gemini.apiKey();
    }

    public String mockModelName() {
        return valueOrDefault(mock == null ? null : mock.modelName(), "mock-model");
    }

    private String openaiModelName(ProviderModelSelector selector) {
        return switch (selector) {
            case FAST -> valueOrDefault(openai == null ? null : openai.fastModel(), "gpt-5-mini");
            case BALANCED -> valueOrDefault(openai == null ? null : openai.balancedModel(), "gpt-5");
            case QUALITY -> valueOrDefault(openai == null ? null : openai.qualityModel(), "gpt-5");
        };
    }

    private String geminiModelName(ProviderModelSelector selector) {
        return switch (selector) {
            case FAST -> valueOrDefault(gemini == null ? null : gemini.fastModel(), "gemini-2.5-flash");
            case BALANCED -> valueOrDefault(gemini == null ? null : gemini.balancedModel(), "gemini-2.5-flash");
            case QUALITY -> valueOrDefault(gemini == null ? null : gemini.qualityModel(), "gemini-2.5-pro");
        };
    }

    private static String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    public enum Provider {
        MOCK,
        OPENAI,
        GEMINI
    }

    public record Mock(String modelName) {
    }

    public record OpenAi(String apiKey, String fastModel, String balancedModel, String qualityModel) {
    }

    public record Gemini(String apiKey, String fastModel, String balancedModel, String qualityModel) {
    }

    private enum ProviderModelSelector {
        FAST,
        BALANCED,
        QUALITY
    }
}
