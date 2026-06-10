package com.example.aichat.debate.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DebateTurnPromptBuilderTest {

    private final DebateTurnPromptBuilder promptBuilder = new DebateTurnPromptBuilder();

    @Test
    void buildDebateTurnPromptIncludesCharacterSnapshot() {
        String prompt = promptBuilder.buildDebateTurnPrompt(
                "부먹 vs 찍먹",
                "탕수육 소스를 부어 먹는 것과 찍어 먹는 것 중 어느 방식이 더 나은가?",
                DebateFormat.PROS_AND_CONS,
                participant(),
                List.of(
                        "1. FAST: 부먹은 소스와 튀김의 조화를 극대화합니다.",
                        "2. QUALITY: 찍먹은 바삭함을 지킵니다."
                ),
                600
        );

        assertThat(prompt)
                .contains("[당신의 참가자 모델]\n모델: FAST")
                .contains("[캐릭터]")
                .contains("이름: 합리주의 미식가")
                .contains("설명: 논리적인 캐릭터")
                .contains("성격: {\"rationality\":90}")
                .contains("말투: {\"tone\":\"차분함\"}")
                .contains("최대 600자 이내로 작성하세요.");
    }

    @Test
    void renderUnsetOptionalCharacterFields() {
        DebateParticipant participant = new DebateParticipant(
                null, 1L, 0, ParticipantModel.MOCK, "캐릭터", null, null, null
        );

        String prompt = promptBuilder.buildDebateTurnPrompt(
                "주제",
                "설명",
                DebateFormat.FREE_DISCUSSION,
                participant,
                List.of(),
                100
        );

        assertThat(prompt)
                .contains("설명: 미설정")
                .contains("성격: 미설정")
                .contains("말투: 미설정");
    }

    @Test
    void rejectBlankTopicTitle() {
        assertThatThrownBy(() -> promptBuilder.buildDebateTurnPrompt(
                "   ",
                "탕수육 소스를 부어 먹는 것과 찍어 먹는 것 중 어느 방식이 더 나은가?",
                DebateFormat.PROS_AND_CONS,
                participant(),
                List.of(),
                600
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("topicTitle is required");
    }

    private static DebateParticipant participant() {
        return new DebateParticipant(
                null,
                10L,
                0,
                ParticipantModel.FAST,
                "합리주의 미식가",
                "논리적인 캐릭터",
                "{\"rationality\":90}",
                "{\"tone\":\"차분함\"}"
        );
    }
}
