package com.example.aichat.generation.infrastructure;

import com.example.aichat.generation.application.GenerationException;
import com.example.aichat.generation.application.GenerationRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeminiTextGeneratorTest {

    @Test
    void generateMapsRequestAndProviderResponse() {
        GeminiTextGenerator generator = new GeminiTextGenerator((modelName, prompt) -> {
            assertThat(modelName).isEqualTo("gemini-2.5-flash");
            assertThat(prompt).isEqualTo("토론 발화를 생성하세요.");
            return new ProviderGenerationResponse("  생성된 발화  ", "gemini-2.5-flash-001", 21, 43);
        });

        var result = generator.generate(new GenerationRequest("토론 발화를 생성하세요.", "gemini-2.5-flash"));

        assertThat(result.content()).isEqualTo("생성된 발화");
        assertThat(result.modelName()).isEqualTo("gemini-2.5-flash-001");
        assertThat(result.inputTokens()).isEqualTo(21);
        assertThat(result.outputTokens()).isEqualTo(43);
    }

    @Test
    void generateWrapsProviderFailure() {
        RuntimeException cause = new RuntimeException("service unavailable");
        GeminiTextGenerator generator = new GeminiTextGenerator((modelName, prompt) -> {
            throw cause;
        });

        assertThatThrownBy(() -> generator.generate(new GenerationRequest("prompt", "gemini-2.5-flash")))
                .isInstanceOf(GenerationException.class)
                .hasMessage("Gemini text generation failed")
                .hasCause(cause)
                .satisfies(exception -> {
                    GenerationException generationException = (GenerationException) exception;
                    assertThat(generationException.provider()).isEqualTo("gemini");
                    assertThat(generationException.modelName()).isEqualTo("gemini-2.5-flash");
                });
    }

    @Test
    void generateRejectsNullRequestAndBlankProviderContent() {
        GeminiTextGenerator generator = new GeminiTextGenerator(
                (modelName, prompt) -> new ProviderGenerationResponse("", modelName, null, null)
        );

        assertThatThrownBy(() -> generator.generate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("request is required");

        assertThatThrownBy(() -> generator.generate(new GenerationRequest("prompt", "gemini-2.5-flash")))
                .isInstanceOf(GenerationException.class)
                .hasMessage("Gemini returned an empty text response");
    }

    @Test
    void createClientRejectsMissingApiKey() {
        assertThatThrownBy(() -> GeminiTextGenerator.createClient(null))
                .isInstanceOf(GenerationException.class)
                .hasMessage("GEMINI_API_KEY is required");
    }
}
