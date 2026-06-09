package com.example.aichat.debate.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DebateTurnPromptBuilderTest {

    private final DebateTurnPromptBuilder promptBuilder = new DebateTurnPromptBuilder();

    @Test
    void buildDebateTurnPromptFormatsPromptAccordingToSpec() {
        String prompt = promptBuilder.buildDebateTurnPrompt(
                "부먹 vs 찍먹",
                "탕수육 소스를 부어 먹는 것과 찍어 먹는 것 중 어느 방식이 더 나은가?",
                DebateFormat.PROS_AND_CONS,
                ParticipantModel.FAST,
                List.of(
                        "1. FAST: 부먹은 소스와 튀김의 조화를 극대화합니다.",
                        "2. QUALITY: 찍먹은 바삭함을 지킵니다."
                ),
                600
        );

        assertThat(prompt).isEqualTo("""
                당신은 AI 토론 플랫폼의 캐릭터입니다.

                [토론 주제]
                부먹 vs 찍먹

                [주제 설명]
                탕수육 소스를 부어 먹는 것과 찍어 먹는 것 중 어느 방식이 더 나은가?

                [토론 형식]
                PROS_AND_CONS

                [당신의 참가자 모델]
                모델: FAST

                [이전 발화]
                1. FAST: 부먹은 소스와 튀김의 조화를 극대화합니다.
                2. QUALITY: 찍먹은 바삭함을 지킵니다.

                [지시]
                위 정보를 바탕으로 참가자 모델의 응답 정책에 맞게 다음 발화를 작성하세요.
                상대의 이전 발화를 참고하되, 단순 반복하지 마세요.
                토론 주제에서 벗어나지 마세요.
                최대 600자 이내로 작성하세요.
                """);
    }

    @Test
    void rejectBlankTopicTitle() {
        assertThatThrownBy(() -> promptBuilder.buildDebateTurnPrompt(
                "   ",
                "탕수육 소스를 부어 먹는 것과 찍어 먹는 것 중 어느 방식이 더 나은가?",
                DebateFormat.PROS_AND_CONS,
                ParticipantModel.FAST,
                List.of(),
                600
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("topicTitle is required");
    }
}
