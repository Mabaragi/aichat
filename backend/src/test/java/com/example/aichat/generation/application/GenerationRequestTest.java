package com.example.aichat.generation.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GenerationRequestTest {

    @Test
    void createCommandTrimsPromptAndModelName() {
        GenerationRequest request = new GenerationRequest("  prompt text  ", "  mock-model  ");

        assertThat(request.prompt()).isEqualTo("prompt text");
        assertThat(request.modelName()).isEqualTo("mock-model");
    }

    @Test
    void rejectBlankPromptOrModelName() {
        assertThatThrownBy(() -> new GenerationRequest("   ", "mock-model"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("prompt is required");

        assertThatThrownBy(() -> new GenerationRequest("prompt", "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("modelName is required");
    }
}
