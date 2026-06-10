package com.example.aichat.generation.infrastructure;

import com.example.aichat.generation.application.GenerationException;
import com.example.aichat.generation.application.GenerationRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpenAiTextGeneratorTest {

    @Test
    void generateMapsRequestAndProviderResponse() {
        OpenAiTextGenerator generator = new OpenAiTextGenerator((modelName, prompt) -> {
            assertThat(modelName).isEqualTo("gpt-5-mini");
            assertThat(prompt).isEqualTo("토론 발화를 생성하세요.");
            return new ProviderGenerationResponse("  생성된 발화  ", "gpt-5-mini-2025-08-07", 12, 34);
        });

        var result = generator.generate(new GenerationRequest("토론 발화를 생성하세요.", "gpt-5-mini"));

        assertThat(result.content()).isEqualTo("생성된 발화");
        assertThat(result.modelName()).isEqualTo("gpt-5-mini-2025-08-07");
        assertThat(result.inputTokens()).isEqualTo(12);
        assertThat(result.outputTokens()).isEqualTo(34);
    }

    @Test
    void generateWrapsProviderFailure() {
        RuntimeException cause = new RuntimeException("rate limited");
        OpenAiTextGenerator generator = new OpenAiTextGenerator((modelName, prompt) -> {
            throw cause;
        });

        assertThatThrownBy(() -> generator.generate(new GenerationRequest("prompt", "gpt-5-mini")))
                .isInstanceOf(GenerationException.class)
                .hasMessage("OpenAI text generation failed")
                .hasCause(cause)
                .satisfies(exception -> {
                    GenerationException generationException = (GenerationException) exception;
                    assertThat(generationException.provider()).isEqualTo("openai");
                    assertThat(generationException.modelName()).isEqualTo("gpt-5-mini");
                });
    }

    @Test
    void generateRejectsNullRequestAndBlankProviderContent() {
        OpenAiTextGenerator generator = new OpenAiTextGenerator(
                (modelName, prompt) -> new ProviderGenerationResponse(" ", modelName, null, null)
        );

        assertThatThrownBy(() -> generator.generate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("request is required");

        assertThatThrownBy(() -> generator.generate(new GenerationRequest("prompt", "gpt-5-mini")))
                .isInstanceOf(GenerationException.class)
                .hasMessage("OpenAI returned an empty text response");
    }

    @Test
    void createClientRejectsMissingApiKey() {
        assertThatThrownBy(() -> OpenAiTextGenerator.createClient(" "))
                .isInstanceOf(GenerationException.class)
                .hasMessage("OPENAI_API_KEY is required");
    }
}
