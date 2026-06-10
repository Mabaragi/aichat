package com.example.aichat.debate.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DebateParticipantTest {

    @Test
    void createParticipantWithCharacterSnapshot() {
        DebateParticipant participant = participant(0, ParticipantModel.FAST);

        assertThat(participant.getSourceCharacterId()).isEqualTo(10L);
        assertThat(participant.getPosition()).isZero();
        assertThat(participant.getModel()).isEqualTo(ParticipantModel.FAST);
        assertThat(participant.getName()).isEqualTo("합리주의 미식가");
        assertThat(participant.getDescription()).isEqualTo("논리적인 캐릭터");
        assertThat(participant.getPersonality()).isEqualTo("{\"rationality\":90}");
        assertThat(participant.getSpeechStyle()).isEqualTo("{\"tone\":\"차분함\"}");
    }

    @Test
    void rejectParticipantWithoutRequiredSnapshotFields() {
        assertThatThrownBy(() -> new DebateParticipant(
                null, null, 0, ParticipantModel.FAST, "이름", null, null, null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("sourceCharacterId is required");

        assertThatThrownBy(() -> new DebateParticipant(
                null, 10L, 0, null, "이름", null, null, null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("model is required");

        assertThatThrownBy(() -> new DebateParticipant(
                null, 10L, 0, ParticipantModel.FAST, " ", null, null, null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name is required");
    }

    @Test
    void rejectOutOfRangePosition() {
        assertThatThrownBy(() -> participant(-1, ParticipantModel.FAST))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("position must be 0 or 1");

        assertThatThrownBy(() -> participant(2, ParticipantModel.FAST))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("position must be 0 or 1");
    }

    private static DebateParticipant participant(int position, ParticipantModel model) {
        return new DebateParticipant(
                null,
                10L,
                position,
                model,
                "합리주의 미식가",
                "논리적인 캐릭터",
                "{\"rationality\":90}",
                "{\"tone\":\"차분함\"}"
        );
    }
}
