package com.example.aichat.debate.application;

import com.example.aichat.common.exception.BusinessException;
import com.example.aichat.common.exception.ErrorCode;
import com.example.aichat.debate.domain.DebateFormat;
import com.example.aichat.debate.domain.DebateParticipant;
import com.example.aichat.debate.domain.DebateSession;
import com.example.aichat.debate.domain.DebateSessionRepository;
import com.example.aichat.debate.domain.ParticipantModel;
import com.example.aichat.support.PersonaFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DebateSessionLifecycleUseCaseTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 6, 10, 12, 0);
    private static final LocalDateTime STARTED_AT = LocalDateTime.of(2026, 6, 10, 12, 1);
    private static final LocalDateTime ENDED_AT = LocalDateTime.of(2026, 6, 10, 12, 10);

    private InMemoryDebateSessionRepository debateSessionRepository;
    private StartDebateSessionUseCase startDebateSessionUseCase;
    private CompleteDebateSessionUseCase completeDebateSessionUseCase;

    @BeforeEach
    void setUp() {
        debateSessionRepository = new InMemoryDebateSessionRepository();
        startDebateSessionUseCase = new StartDebateSessionUseCase(
                debateSessionRepository,
                () -> STARTED_AT
        );
        completeDebateSessionUseCase = new CompleteDebateSessionUseCase(
                debateSessionRepository,
                () -> ENDED_AT
        );
    }

    @Test
    void startSessionTransitionsToRunningAndReturnsSummary() {
        DebateSession session = debateSessionRepository.save(createdSession());

        DebateSessionLifecycleView started = startDebateSessionUseCase.execute(
                new StartDebateSessionCommand(authenticatedActor(), session.getId())
        );

        assertThat(started.id()).isEqualTo(session.getId());
        assertThat(started.status()).isEqualTo(com.example.aichat.debate.domain.DebateSessionStatus.RUNNING);
        assertThat(started.startedAt()).isEqualTo(STARTED_AT);
        assertThat(debateSessionRepository.findById(session.getId())).hasValueSatisfying(saved -> {
            assertThat(saved.getStatus()).isEqualTo(com.example.aichat.debate.domain.DebateSessionStatus.RUNNING);
            assertThat(saved.getStartedAt()).isEqualTo(STARTED_AT);
        });
    }

    @Test
    void completeSessionTransitionsToCompletedAndReturnsSummary() {
        DebateSession session = createdSession();
        session.start(STARTED_AT);
        session = debateSessionRepository.save(session);
        Long sessionId = session.getId();

        DebateSessionLifecycleView completed = completeDebateSessionUseCase.execute(
                new CompleteDebateSessionCommand(authenticatedActor(), sessionId)
        );

        assertThat(completed.id()).isEqualTo(sessionId);
        assertThat(completed.status()).isEqualTo(com.example.aichat.debate.domain.DebateSessionStatus.COMPLETED);
        assertThat(completed.endedAt()).isEqualTo(ENDED_AT);
        assertThat(debateSessionRepository.findById(sessionId)).hasValueSatisfying(saved -> {
            assertThat(saved.getStatus()).isEqualTo(com.example.aichat.debate.domain.DebateSessionStatus.COMPLETED);
            assertThat(saved.getEndedAt()).isEqualTo(ENDED_AT);
        });
    }

    @Test
    void rejectStartingSessionWhenItIsAlreadyRunning() {
        DebateSession session = createdSession();
        session.start(STARTED_AT);
        session = debateSessionRepository.save(session);
        Long sessionId = session.getId();

        assertThatThrownBy(() -> startDebateSessionUseCase.execute(
                new StartDebateSessionCommand(authenticatedActor(), sessionId)
        ))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getCode())
                        .isEqualTo(ErrorCode.INVALID_SESSION_STATE));
    }

    @Test
    void rejectCompletingSessionWhenItIsNotRunning() {
        DebateSession session = debateSessionRepository.save(createdSession());

        assertThatThrownBy(() -> completeDebateSessionUseCase.execute(
                new CompleteDebateSessionCommand(authenticatedActor(), session.getId())
        ))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getCode())
                        .isEqualTo(ErrorCode.INVALID_SESSION_STATE));
    }

    private static DebateSession createdSession() {
        return DebateSession.create(
                1L,
                "부먹 vs 찍먹",
                "어느 방식이 더 나은가?",
                "FOOD",
                DebateFormat.PROS_AND_CONS,
                5,
                600,
                List.of(
                        DebateParticipant.create(
                                10L,
                                0,
                                ParticipantModel.FAST,
                                "첫 번째 캐릭터",
                                "논리적인 캐릭터",
                                PersonaFixtures.rationalGourmetJson()
                        ),
                        DebateParticipant.create(
                                20L,
                                1,
                                ParticipantModel.QUALITY,
                                "두 번째 캐릭터",
                                "직관적인 캐릭터",
                                PersonaFixtures.empatheticJson()
                        )
                ),
                CREATED_AT
        );
    }

    private static com.example.aichat.common.security.RequestActor authenticatedActor() {
        return com.example.aichat.common.security.RequestActor.authenticated(1L);
    }

    private static final class InMemoryDebateSessionRepository implements DebateSessionRepository {
        private final AtomicLong nextId = new AtomicLong(1L);
        private final Map<Long, DebateSession> sessions = new HashMap<>();

        @Override
        public DebateSession save(DebateSession session) {
            long sessionId = session.getId() == null ? nextId.getAndIncrement() : session.getId();
            DebateSession saved = new DebateSession(
                    sessionId,
                    session.getOwnerId(),
                    session.getTopicTitle(),
                    session.getTopicDescription(),
                    session.getTopicCategory(),
                    session.getStatus(),
                    session.getFormat(),
                    session.getMaxRounds(),
                    session.getCurrentRound(),
                    session.getMaxTurnLength(),
                    session.getParticipants(),
                    session.getCreatedAt(),
                    session.getStartedAt(),
                    session.getEndedAt()
            );
            sessions.put(sessionId, saved);
            return saved;
        }

        @Override
        public Optional<DebateSession> findById(Long sessionId) {
            return Optional.ofNullable(sessions.get(sessionId));
        }
    }
}
