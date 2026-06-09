package com.example.aichat.debate.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DebateParticipantTest {

    @Test
    void createParticipantWithModel() {
        DebateParticipant participant = new DebateParticipant(ParticipantModel.FAST);

        assertThat(participant.getModel()).isEqualTo(ParticipantModel.FAST);
    }

    @Test
    void rejectParticipantWithoutModel() {
        assertThatThrownBy(() -> new DebateParticipant(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("model is required");
    }
}
