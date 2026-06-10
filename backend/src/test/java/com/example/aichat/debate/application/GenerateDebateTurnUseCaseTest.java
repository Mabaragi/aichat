package com.example.aichat.debate.application;

import com.example.aichat.common.exception.BusinessException;
import com.example.aichat.common.exception.ErrorCode;
import com.example.aichat.common.security.RequestActor;
import com.example.aichat.common.time.TimeProvider;
import com.example.aichat.debate.domain.DebateFormat;
import com.example.aichat.debate.domain.DebateParticipant;
import com.example.aichat.debate.domain.DebateSession;
import com.example.aichat.debate.domain.DebateSessionRepository;
import com.example.aichat.debate.domain.DebateSessionStatus;
import com.example.aichat.debate.domain.DebateTurn;
import com.example.aichat.debate.domain.DebateTurnPromptBuilder;
import com.example.aichat.debate.domain.DebateTurnRepository;
import com.example.aichat.debate.domain.ParticipantModel;
import com.example.aichat.debate.domain.TurnStatus;
import com.example.aichat.debate.domain.TurnType;
import com.example.aichat.generation.application.GenerationRequest;
import com.example.aichat.generation.application.GenerationResult;
import com.example.aichat.generation.application.TextGenerator;
import com.example.aichat.generation.infrastructure.GenerationProperties;
import com.example.aichat.generation.infrastructure.MockTextGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GenerateDebateTurnUseCaseTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 6, 10, 12, 0);
    private static final LocalDateTime STARTED_AT = LocalDateTime.of(2026, 6, 10, 12, 1);
    private static final LocalDateTime FIRST_TURN_AT = LocalDateTime.of(2026, 6, 10, 12, 2);
    private static final LocalDateTime SECOND_TURN_AT = LocalDateTime.of(2026, 6, 10, 12, 3);

    private final List<GenerationRequest> providerRequests = new ArrayList<>();

    private InMemoryDebateSessionRepository debateSessionRepository;
    private InMemoryDebateTurnRepository debateTurnRepository;
    private StartDebateSessionUseCase startDebateSessionUseCase;
    private GenerateDebateTurnUseCase useCase;

    @BeforeEach
    void setUp() {
        debateSessionRepository = new InMemoryDebateSessionRepository();
        debateTurnRepository = new InMemoryDebateTurnRepository();

        TimeProvider timeProvider = new SequenceTimeProvider(
                STARTED_AT,
                FIRST_TURN_AT,
                SECOND_TURN_AT
        );

        startDebateSessionUseCase = new StartDebateSessionUseCase(
                debateSessionRepository,
                timeProvider
        );
        useCase = new GenerateDebateTurnUseCase(
                debateSessionRepository,
                debateTurnRepository,
                new GenerateNextTurnUseCase(),
                new DebateTurnPromptBuilder(),
                request -> {
                    providerRequests.add(request);
                    return new GenerationResult(
                            "FAST provider response",
                            "gpt-5-mini-2025-08-07",
                            10,
                            5
                    );
                },
                new MockTextGenerator(),
                generationProperties(),
                timeProvider
        );
    }

    @Test
    void generateTurnUsesMockForMockParticipantAndActiveProviderForOtherParticipants() {
        DebateSession session = debateSessionRepository.save(sessionFixture());
        startDebateSessionUseCase.execute(new StartDebateSessionCommand(actor(), session.getId()));

        DebateTurnView firstTurn = useCase.execute(new GenerateDebateTurnCommand(actor(), session.getId()));
        DebateTurnView secondTurn = useCase.execute(new GenerateDebateTurnCommand(actor(), session.getId()));

        assertThat(firstTurn.participantModel()).isEqualTo(ParticipantModel.MOCK);
        assertThat(firstTurn.turnIndex()).isEqualTo(1);
        assertThat(firstTurn.round()).isEqualTo(1);
        assertThat(firstTurn.type()).isEqualTo(TurnType.ARGUMENT);
        assertThat(firstTurn.status()).isEqualTo(TurnStatus.COMPLETED);
        assertThat(firstTurn.content()).isEqualTo(
                "MOCK 참가자가 '부먹 vs 찍먹'에 대해 다음 주장을 생성합니다."
        );
        assertThat(firstTurn.modelName()).isEqualTo("mock-model");

        assertThat(secondTurn.participantModel()).isEqualTo(ParticipantModel.FAST);
        assertThat(secondTurn.turnIndex()).isEqualTo(2);
        assertThat(secondTurn.round()).isEqualTo(1);
        assertThat(secondTurn.type()).isEqualTo(TurnType.REBUTTAL);
        assertThat(secondTurn.status()).isEqualTo(TurnStatus.COMPLETED);
        assertThat(secondTurn.content()).isEqualTo("FAST provider response");
        assertThat(secondTurn.modelName()).isEqualTo("gpt-5-mini-2025-08-07");

        assertThat(providerRequests).hasSize(1);
        assertThat(providerRequests.get(0).modelName()).isEqualTo("gpt-5-mini");
        assertThat(providerRequests.get(0).prompt())
                .contains("[당신의 참가자 모델]")
                .contains("모델: FAST")
                .contains("1. MOCK: MOCK 참가자가 '부먹 vs 찍먹'에 대해 다음 주장을 생성합니다.");

        DebateSession storedSession = debateSessionRepository.findById(session.getId()).orElseThrow();
        assertThat(storedSession.getStatus()).isEqualTo(DebateSessionStatus.COMPLETED);
        assertThat(storedSession.getCurrentRound()).isEqualTo(1);

        assertThat(debateTurnRepository.findBySessionIdOrderByTurnIndexAsc(session.getId()))
                .extracting(DebateTurn::getTurnIndex)
                .containsExactly(1, 2);
    }

    @Test
    void rejectGenerationAfterSessionCompleted() {
        DebateSession session = debateSessionRepository.save(sessionFixture());
        startDebateSessionUseCase.execute(new StartDebateSessionCommand(actor(), session.getId()));
        useCase.execute(new GenerateDebateTurnCommand(actor(), session.getId()));
        useCase.execute(new GenerateDebateTurnCommand(actor(), session.getId()));

        assertThatThrownBy(() -> useCase.execute(new GenerateDebateTurnCommand(actor(), session.getId())))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getCode())
                        .isEqualTo(ErrorCode.INVALID_SESSION_STATE));
    }

    private static DebateSession sessionFixture() {
        return DebateSession.create(
                1L,
                "부먹 vs 찍먹",
                "어느 방식이 더 나은가?",
                "FOOD",
                DebateFormat.PROS_AND_CONS,
                1,
                600,
                List.of(
                        new DebateParticipant(
                                1L,
                                10L,
                                0,
                                ParticipantModel.MOCK,
                                "MOCK 참가자",
                                "mock speaker",
                                "{\"mood\":\"calm\"}",
                                "{\"tone\":\"plain\"}"
                        ),
                        new DebateParticipant(
                                2L,
                                20L,
                                1,
                                ParticipantModel.FAST,
                                "FAST 참가자",
                                "fast speaker",
                                "{\"mood\":\"sharp\"}",
                                "{\"tone\":\"sharp\"}"
                        )
                ),
                CREATED_AT
        );
    }

    private static RequestActor actor() {
        return RequestActor.authenticated(1L);
    }

    private static GenerationProperties generationProperties() {
        return new GenerationProperties(
                GenerationProperties.Provider.OPENAI,
                new GenerationProperties.Mock("mock-model"),
                new GenerationProperties.OpenAi(
                        "openai-secret",
                        "gpt-5-mini",
                        "gpt-5",
                        "gpt-5"
                ),
                new GenerationProperties.Gemini(
                        "gemini-secret",
                        "gemini-2.5-flash",
                        "gemini-2.5-flash",
                        "gemini-2.5-pro"
                )
        );
    }

    private static final class SequenceTimeProvider implements TimeProvider {
        private final List<LocalDateTime> times;
        private int index;

        private SequenceTimeProvider(LocalDateTime... times) {
            this.times = List.of(times);
        }

        @Override
        public LocalDateTime now() {
            return times.get(index++);
        }
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

    private static final class InMemoryDebateTurnRepository implements DebateTurnRepository {
        private final AtomicLong nextId = new AtomicLong(1L);
        private final Map<Long, List<DebateTurn>> turnsBySession = new HashMap<>();

        @Override
        public DebateTurn save(DebateTurn turn) {
            long turnId = turn.getId() == null ? nextId.getAndIncrement() : turn.getId();
            DebateTurn saved = new DebateTurn(
                    turnId,
                    turn.getSessionId(),
                    turn.getParticipantId(),
                    turn.getRound(),
                    turn.getTurnIndex(),
                    turn.getType(),
                    turn.getStatus(),
                    turn.getContent(),
                    turn.getPromptSnapshot(),
                    turn.getModelName(),
                    turn.getInputTokens(),
                    turn.getOutputTokens(),
                    turn.getCreatedAt()
            );
            turnsBySession.computeIfAbsent(turn.getSessionId(), ignored -> new ArrayList<>()).add(saved);
            turnsBySession.get(turn.getSessionId())
                    .sort((left, right) -> Integer.compare(left.getTurnIndex(), right.getTurnIndex()));
            return saved;
        }

        @Override
        public List<DebateTurn> findBySessionIdOrderByTurnIndexAsc(Long sessionId) {
            return turnsBySession.getOrDefault(sessionId, List.of());
        }
    }
}
