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
import com.example.aichat.generation.application.GenerationException;
import com.example.aichat.generation.application.GenerationRequest;
import com.example.aichat.generation.application.GenerationResult;
import com.example.aichat.generation.application.TextGenerator;
import com.example.aichat.generation.infrastructure.GenerationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GenerateDebateTurnUseCase {

    private final DebateSessionRepository debateSessionRepository;
    private final DebateTurnRepository debateTurnRepository;
    private final GenerateNextTurnUseCase generateNextTurnUseCase;
    private final DebateTurnPromptBuilder promptBuilder;
    private final TextGenerator textGenerator;
    private final com.example.aichat.generation.infrastructure.MockTextGenerator mockTextGenerator;
    private final GenerationProperties generationProperties;
    private final TimeProvider timeProvider;

    public GenerateDebateTurnUseCase(DebateSessionRepository debateSessionRepository,
                                     DebateTurnRepository debateTurnRepository,
                                     GenerateNextTurnUseCase generateNextTurnUseCase,
                                     DebateTurnPromptBuilder promptBuilder,
                                     TextGenerator textGenerator,
                                     com.example.aichat.generation.infrastructure.MockTextGenerator mockTextGenerator,
                                     GenerationProperties generationProperties,
                                     TimeProvider timeProvider) {
        this.debateSessionRepository = debateSessionRepository;
        this.debateTurnRepository = debateTurnRepository;
        this.generateNextTurnUseCase = generateNextTurnUseCase;
        this.promptBuilder = promptBuilder;
        this.textGenerator = textGenerator;
        this.mockTextGenerator = mockTextGenerator;
        this.generationProperties = generationProperties;
        this.timeProvider = timeProvider;
    }

    @Transactional
    public DebateTurnView execute(GenerateDebateTurnCommand command) {
        DebateSession session = requireSession(command.actor(), command.sessionId());
        if (session.getStatus() != DebateSessionStatus.RUNNING || !session.canGenerateTurn()) {
            throw new BusinessException(
                    ErrorCode.INVALID_SESSION_STATE,
                    "session must be RUNNING to generate turns"
            );
        }

        List<DebateTurn> turns = debateTurnRepository.findBySessionIdOrderByTurnIndexAsc(session.getId());
        int participantCount = session.getParticipants().size();
        int turnIndex = turns.size() + 1;
        int maxTurnCount = session.getMaxRounds() * participantCount;
        if (turnIndex > maxTurnCount) {
            throw new BusinessException(
                    ErrorCode.INVALID_SESSION_STATE,
                    "session has already reached the maximum turn count"
            );
        }

        int speakerIndex = generateNextTurnUseCase.resolveSpeakerIndex(turnIndex, participantCount);
        int round = generateNextTurnUseCase.resolveRound(turnIndex, participantCount);
        TurnType type = resolveTurnType(session.getFormat(), turnIndex);

        Map<Long, DebateParticipant> participantsById = session.getParticipants().stream()
                .collect(Collectors.toMap(DebateParticipant::getId, Function.identity()));
        DebateParticipant participant = session.getParticipants().stream()
                .sorted(Comparator.comparingInt(DebateParticipant::getPosition))
                .toList()
                .get(speakerIndex);

        List<String> previousTurns = turns.stream()
                .map(previousTurn -> formatPreviousTurn(
                        previousTurn,
                        requireParticipant(participantsById, previousTurn.getParticipantId())
                ))
                .toList();

        String prompt = promptBuilder.buildDebateTurnPrompt(
                session.getTopicTitle(),
                session.getTopicDescription(),
                session.getFormat(),
                participant,
                previousTurns,
                session.getMaxTurnLength()
        );

        GenerationResult generationResult = generate(participant.getModel(), prompt);
        DebateTurn turn = DebateTurn.create(
                session.getId(),
                participant.getId(),
                round,
                turnIndex,
                type,
                TurnStatus.COMPLETED,
                generationResult.content(),
                prompt,
                generationResult.modelName(),
                generationResult.inputTokens(),
                generationResult.outputTokens(),
                timeProvider.now()
        );

        DebateTurn savedTurn = debateTurnRepository.save(turn);
        session.registerTurn(turnIndex, savedTurn.getCreatedAt());
        debateSessionRepository.save(session);

        return DebateTurnView.from(savedTurn, participant.getModel());
    }

    private DebateSession requireSession(RequestActor actor, Long sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("sessionId is required");
        }

        DebateSession session = debateSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DEBATE_SESSION_NOT_FOUND,
                        "Debate session not found or inaccessible: " + sessionId
                ));

        if (!actor.canManage(session.getOwnerId())) {
            throw new BusinessException(
                    ErrorCode.DEBATE_SESSION_NOT_FOUND,
                    "Debate session not found or inaccessible: " + sessionId
            );
        }

        return session;
    }

    private GenerationResult generate(ParticipantModel participantModel, String prompt) {
        GenerationRequest request = new GenerationRequest(prompt, generationProperties.resolveModelName(participantModel));
        try {
            if (participantModel == ParticipantModel.MOCK) {
                return mockTextGenerator.generate(request);
            }
            return textGenerator.generate(request);
        } catch (GenerationException exception) {
            throw new BusinessException(
                    ErrorCode.TURN_GENERATION_FAILED,
                    "Turn generation failed via " + exception.provider() + ": " + exception.getMessage()
            );
        }
    }

    private static TurnType resolveTurnType(DebateFormat format, int turnIndex) {
        if (format == DebateFormat.PROS_AND_CONS) {
            return turnIndex % 2 == 1 ? TurnType.ARGUMENT : TurnType.REBUTTAL;
        }
        return TurnType.ARGUMENT;
    }

    private static String formatPreviousTurn(DebateTurn turn, DebateParticipant participant) {
        return turn.getTurnIndex() + ". " + participant.getModel().name() + ": " + turn.getContent();
    }

    private static DebateParticipant requireParticipant(
            Map<Long, DebateParticipant> participants,
            Long participantId
    ) {
        DebateParticipant participant = participants.get(participantId);
        if (participant == null) {
            throw new BusinessException(
                    ErrorCode.DEBATE_PARTICIPANT_NOT_FOUND,
                    "Debate participant not found: " + participantId
            );
        }
        return participant;
    }
}
