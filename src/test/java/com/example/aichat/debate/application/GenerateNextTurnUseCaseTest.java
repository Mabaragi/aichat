package com.example.aichat.debate.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GenerateNextTurnUseCaseTest {

    private final GenerateNextTurnUseCase useCase = new GenerateNextTurnUseCase();

    @Test
    void resolveSpeakerIndexAlternatesBetweenTwoParticipants() {
        assertThat(useCase.resolveSpeakerIndex(1, 2)).isEqualTo(0);
        assertThat(useCase.resolveSpeakerIndex(2, 2)).isEqualTo(1);
        assertThat(useCase.resolveSpeakerIndex(3, 2)).isEqualTo(0);
    }

    @Test
    void resolveRoundGroupsTwoTurnsPerRound() {
        assertThat(useCase.resolveRound(1, 2)).isEqualTo(1);
        assertThat(useCase.resolveRound(2, 2)).isEqualTo(1);
        assertThat(useCase.resolveRound(3, 2)).isEqualTo(2);
        assertThat(useCase.resolveRound(4, 2)).isEqualTo(2);
    }

    @Test
    void shouldCompleteSessionWhenMaxTurnCountIsReached() {
        assertThat(useCase.shouldCompleteSession(9, 5, 2)).isFalse();
        assertThat(useCase.shouldCompleteSession(10, 5, 2)).isTrue();
    }

    @Test
    void rejectInvalidIndexesAndCounts() {
        assertThatThrownBy(() -> useCase.resolveSpeakerIndex(0, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("turnIndex must be at least 1");

        assertThatThrownBy(() -> useCase.resolveRound(1, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("participantCount must be at least 2");

        assertThatThrownBy(() -> useCase.shouldCompleteSession(1, 0, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("maxRounds must be at least 1");
    }
}
