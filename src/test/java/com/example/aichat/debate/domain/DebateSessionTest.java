package com.example.aichat.debate.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DebateSessionTest {

    @Test
    void createSessionWithTwoParticipants() {
        DebateSession session = sessionFixture().create();

        assertThat(session.getOwnerId()).isEqualTo(1L);
        assertThat(session.getTopicTitle()).isEqualTo("부먹 vs 찍먹");
        assertThat(session.getStatus()).isEqualTo(DebateSessionStatus.CREATED);
        assertThat(session.getFormat()).isEqualTo(DebateFormat.PROS_AND_CONS);
        assertThat(session.getMaxRounds()).isEqualTo(5);
        assertThat(session.getCurrentRound()).isZero();
        assertThat(session.getMaxTurnLength()).isEqualTo(600);
        assertThat(session.getParticipants())
                .extracting(DebateParticipant::getModel)
                .containsExactly(ParticipantModel.FAST, ParticipantModel.QUALITY);
        assertThat(session.getParticipants())
                .extracting(DebateParticipant::getPosition)
                .containsExactly(0, 1);
        assertThat(session.getCreatedAt()).isEqualTo(defaultNow());
    }

    @Test
    void rejectSessionWithLessThanTwoParticipants() {
        DebateSessionFixture fixture = sessionFixture()
                .participants(List.of(participant(ParticipantModel.FAST)));

        assertThatThrownBy(fixture::create)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("participants must be exactly 2");
    }

    @Test
    void rejectSessionWithMoreThanTwoParticipants() {
        DebateSessionFixture fixture = sessionFixture()
                .participants(List.of(
                        participant(0, ParticipantModel.FAST),
                        participant(1, ParticipantModel.BALANCED),
                        participant(1, ParticipantModel.QUALITY)
                ));

        assertThatThrownBy(fixture::create)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("participants must be exactly 2");
    }

    @Test
    void rejectParticipantsWithoutPositionsZeroAndOne() {
        DebateSessionFixture fixture = sessionFixture()
                .participants(List.of(
                        participant(0, ParticipantModel.FAST),
                        participant(0, ParticipantModel.QUALITY)
                ));

        assertThatThrownBy(fixture::create)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("participant positions must be 0 and 1");
    }

    @Test
    void rejectSessionWithOutOfRangeMaxRounds() {
        DebateSessionFixture tooSmall = sessionFixture().maxRounds(0);
        DebateSessionFixture tooLarge = sessionFixture().maxRounds(11);

        assertThatThrownBy(tooSmall::create)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("maxRounds out of range");

        assertThatThrownBy(tooLarge::create)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("maxRounds out of range");
    }

    @Test
    void rejectSessionWithOutOfRangeMaxTurnLength() {
        DebateSessionFixture tooSmall = sessionFixture().maxTurnLength(99);
        DebateSessionFixture tooLarge = sessionFixture().maxTurnLength(2001);

        assertThatThrownBy(tooSmall::create)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("maxTurnLength out of range");

        assertThatThrownBy(tooLarge::create)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("maxTurnLength out of range");
    }

    @Test
    void startCreatedSession() {
        DebateSession session = sessionFixture().create();
        LocalDateTime startedAt = defaultNow().plusMinutes(1);

        session.start(startedAt);

        assertThat(session.getStatus()).isEqualTo(DebateSessionStatus.RUNNING);
        assertThat(session.getStartedAt()).isEqualTo(startedAt);
        assertThat(session.canGenerateTurn()).isTrue();
    }

    @Test
    void rejectStartingSessionTwice() {
        DebateSession session = sessionFixture().create();
        session.start(defaultNow().plusMinutes(1));

        assertThatThrownBy(() -> session.start(defaultNow().plusMinutes(2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("session must be CREATED to start");
    }

    @Test
    void onlyRunningSessionCanGenerateTurns() {
        DebateSession session = sessionFixture().create();

        assertThat(session.canGenerateTurn()).isFalse();

        session.start(defaultNow().plusMinutes(1));

        assertThat(session.canGenerateTurn()).isTrue();
    }

    @Test
    void finalTurnCompletesSessionAndSetsCurrentRound() {
        DebateSession session = sessionFixture().create();
        LocalDateTime startedAt = defaultNow().plusMinutes(1);
        LocalDateTime finishedAt = defaultNow().plusMinutes(2);

        session.start(startedAt);
        session.registerTurn(10, finishedAt);

        assertThat(session.getCurrentRound()).isEqualTo(5);
        assertThat(session.getStatus()).isEqualTo(DebateSessionStatus.COMPLETED);
        assertThat(session.getEndedAt()).isEqualTo(finishedAt);
        assertThat(session.isCompleted()).isTrue();
        assertThat(session.canGenerateTurn()).isFalse();
    }

    private static DebateParticipant participant(ParticipantModel model) {
        return participant(model == ParticipantModel.FAST ? 0 : 1, model);
    }

    private static DebateParticipant participant(int position, ParticipantModel model) {
        return new DebateParticipant(
                null,
                10L,
                position,
                model,
                "캐릭터 " + position,
                "설명",
                "{\"rationality\":90}",
                "{\"tone\":\"차분함\"}"
        );
    }

    private static LocalDateTime defaultNow() {
        return LocalDateTime.of(2026, 6, 9, 12, 0);
    }

    private static List<DebateParticipant> defaultParticipants() {
        return List.of(
                participant(ParticipantModel.FAST),
                participant(ParticipantModel.QUALITY)
        );
    }

    private static DebateSessionFixture sessionFixture() {
        return new DebateSessionFixture();
    }

    private static class DebateSessionFixture {
        private Long ownerId = 1L;
        private String topicTitle = "부먹 vs 찍먹";
        private String topicDescription = "탕수육 소스를 부어 먹는 것과 찍어 먹는 것 중 어느 방식이 더 나은가?";
        private String topicCategory = "FOOD";
        private DebateFormat format = DebateFormat.PROS_AND_CONS;
        private int maxRounds = 5;
        private Integer maxTurnLength = 600;
        private List<DebateParticipant> participants = defaultParticipants();
        private LocalDateTime createdAt = defaultNow();

        DebateSessionFixture maxRounds(int maxRounds) {
            this.maxRounds = maxRounds;
            return this;
        }

        DebateSessionFixture maxTurnLength(Integer maxTurnLength) {
            this.maxTurnLength = maxTurnLength;
            return this;
        }

        DebateSessionFixture participants(List<DebateParticipant> participants) {
            this.participants = participants;
            return this;
        }

        DebateSession create() {
            return DebateSession.create(
                    ownerId,
                    topicTitle,
                    topicDescription,
                    topicCategory,
                    format,
                    maxRounds,
                    maxTurnLength,
                    participants,
                    createdAt
            );
        }
    }
}
