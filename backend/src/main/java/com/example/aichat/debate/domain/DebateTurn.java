package com.example.aichat.debate.domain;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class DebateTurn {

    private final Long id;
    private final Long sessionId;
    private final Long participantId;
    private final int round;
    private final int turnIndex;
    private final TurnType type;
    private final TurnStatus status;
    private final String content;
    private final String promptSnapshot;
    private final String modelName;
    private final Integer inputTokens;
    private final Integer outputTokens;
    private final LocalDateTime createdAt;

    public DebateTurn(Long id, Long sessionId, Long participantId, int round, int turnIndex,
                      TurnType type, TurnStatus status, String content, String promptSnapshot,
                      String modelName, Integer inputTokens, Integer outputTokens,
                      LocalDateTime createdAt) {
        validateSessionId(sessionId);
        validateParticipantId(participantId);
        validateRound(round);
        validateTurnIndex(turnIndex);
        validateType(type);
        validateStatus(status);
        validateCreatedAt(createdAt);
        validateContent(status, content);

        this.id = id;
        this.sessionId = sessionId;
        this.participantId = participantId;
        this.round = round;
        this.turnIndex = turnIndex;
        this.type = type;
        this.status = status;
        this.content = content;
        this.promptSnapshot = promptSnapshot;
        this.modelName = modelName;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.createdAt = createdAt;
    }

    public static DebateTurn create(Long sessionId, Long participantId, int round,
                                    int turnIndex, TurnType type, TurnStatus status,
                                    String content, String promptSnapshot, String modelName,
                                    Integer inputTokens, Integer outputTokens,
                                    LocalDateTime createdAt) {
        return new DebateTurn(null, sessionId, participantId, round, turnIndex, type, status,
                content, promptSnapshot, modelName, inputTokens, outputTokens, createdAt);
    }

    private static void validateSessionId(Long sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("sessionId is required");
        }
    }

    private static void validateParticipantId(Long participantId) {
        if (participantId == null) {
            throw new IllegalArgumentException("participantId is required");
        }
    }

    private static void validateRound(int round) {
        if (round < 1) {
            throw new IllegalArgumentException("round must be at least 1");
        }
    }

    private static void validateTurnIndex(int turnIndex) {
        if (turnIndex < 1) {
            throw new IllegalArgumentException("turnIndex must be at least 1");
        }
    }

    private static void validateType(TurnType type) {
        if (type == null) {
            throw new IllegalArgumentException("type is required");
        }
    }

    private static void validateStatus(TurnStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("status is required");
        }
    }

    private static void validateCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt is required");
        }
    }

    private static void validateContent(TurnStatus status, String content) {
        if (status == TurnStatus.COMPLETED && (content == null || content.isBlank())) {
            throw new IllegalArgumentException("content is required for completed turn");
        }
    }
}
