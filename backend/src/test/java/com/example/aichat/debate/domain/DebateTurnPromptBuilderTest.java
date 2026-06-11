package com.example.aichat.debate.domain;

import com.example.aichat.support.PersonaFixtures;
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
                .contains("[페르소나 규칙]")
                .contains("정체성: 합리주의 미식가")
                .contains("핵심 가치: 실증성, 논리")
                .contains("반드시 할 것: 상대 주장을 먼저 요약한다., 불확실한 사실은 단정하지 않는다.")
                .contains("하지 말 것: 인신공격하지 않는다., 출처 없는 수치를 만들지 않는다.")
                .contains("페르소나보다 사실성, 안전, 토론 규칙을 우선하세요.")
                .contains("최대 600자 이내로 작성하세요.")
                .contains("[출력 형식]")
                .contains("[요약]")
                .contains("[입장 상태]");
    }

    @Test
    void renderUnsetOptionalCharacterFields() {
        DebateParticipant participant = new DebateParticipant(
                null, 1L, 0, ParticipantModel.MOCK, "캐릭터", null,
                PersonaFixtures.rationalGourmetJson()
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
                .contains("정체성: 합리주의 미식가");
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
                PersonaFixtures.rationalGourmetJson()
        );
    }
}
