package com.example.aichat.generation.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GenerationResultTest {

    @Test
    void createResultStoresContentModelAndTokenUsage() {
        GenerationResult result = new GenerationResult("  FAST 응답입니다.  ", "  mock-model  ", 12, 24);

        assertThat(result.content()).isEqualTo("FAST 응답입니다.");
        assertThat(result.modelName()).isEqualTo("mock-model");
        assertThat(result.inputTokens()).isEqualTo(12);
        assertThat(result.outputTokens()).isEqualTo(24);
    }

    @Test
    void rejectNegativeTokenUsage() {
        assertThatThrownBy(() -> new GenerationResult("content", "mock-model", -1, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("inputTokens must be at least 0");

        assertThatThrownBy(() -> new GenerationResult("content", "mock-model", 0, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("outputTokens must be at least 0");
    }
}
