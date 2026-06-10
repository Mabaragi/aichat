package com.example.aichat.debate.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DebateTurnTest {

    @Test
    void createCompletedTurnWithContent() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 6, 9, 12, 0);

        DebateTurn turn = DebateTurn.create(
                1L,
                2L,
                1,
                1,
                TurnType.ARGUMENT,
                TurnStatus.COMPLETED,
                "부먹은 소스와 튀김의 조화를 극대화합니다.",
                "prompt snapshot",
                "mock-model",
                12,
                24,
                createdAt
        );

        assertThat(turn.getSessionId()).isEqualTo(1L);
        assertThat(turn.getParticipantId()).isEqualTo(2L);
        assertThat(turn.getRound()).isEqualTo(1);
        assertThat(turn.getTurnIndex()).isEqualTo(1);
        assertThat(turn.getType()).isEqualTo(TurnType.ARGUMENT);
        assertThat(turn.getStatus()).isEqualTo(TurnStatus.COMPLETED);
        assertThat(turn.getContent()).isEqualTo("부먹은 소스와 튀김의 조화를 극대화합니다.");
        assertThat(turn.getPromptSnapshot()).isEqualTo("prompt snapshot");
        assertThat(turn.getModelName()).isEqualTo("mock-model");
        assertThat(turn.getInputTokens()).isEqualTo(12);
        assertThat(turn.getOutputTokens()).isEqualTo(24);
        assertThat(turn.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void rejectCompletedTurnWithoutContent() {
        assertThatThrownBy(() -> DebateTurn.create(
                1L,
                2L,
                1,
                1,
                TurnType.ARGUMENT,
                TurnStatus.COMPLETED,
                "   ",
                null,
                null,
                null,
                null,
                LocalDateTime.of(2026, 6, 9, 12, 0)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("content is required for completed turn");
    }
}
