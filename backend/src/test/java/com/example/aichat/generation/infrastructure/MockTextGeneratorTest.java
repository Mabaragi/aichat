package com.example.aichat.generation.infrastructure;

import com.example.aichat.generation.application.GenerationRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MockTextGeneratorTest {

    private final MockTextGenerator textGenerator = new MockTextGenerator();

    @Test
    void generateReturnsDeterministicMockResponseFromPrompt() {
        String prompt = """
                [토론 주제]
                부먹 vs 찍먹

                [당신의 참가자 모델]
                모델: FAST
                """;

        var result = textGenerator.generate(new GenerationRequest(prompt, "mock-model"));

        assertThat(result.content()).isEqualTo("FAST 참가자가 '부먹 vs 찍먹'에 대해 다음 주장을 생성합니다.");
        assertThat(result.modelName()).isEqualTo("mock-model");
        assertThat(result.inputTokens()).isZero();
        assertThat(result.outputTokens()).isZero();
    }

    @Test
    void generateRejectsNullRequest() {
        assertThatThrownBy(() -> textGenerator.generate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("request is required");
    }
}
