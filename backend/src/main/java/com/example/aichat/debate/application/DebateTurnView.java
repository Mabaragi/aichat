package com.example.aichat.debate.application;

import com.example.aichat.debate.domain.DebateTurn;
import com.example.aichat.debate.domain.ParticipantModel;
import com.example.aichat.debate.domain.TurnStatus;
import com.example.aichat.debate.domain.TurnType;

import java.time.LocalDateTime;

public record DebateTurnView(
        Long id,
        Long sessionId,
        Long participantId,
        ParticipantModel participantModel,
        int round,
        int turnIndex,
        TurnType type,
        TurnStatus status,
        String content,
        String promptSnapshot,
        String modelName,
        Integer inputTokens,
        Integer outputTokens,
        LocalDateTime createdAt
) {

    public static DebateTurnView from(DebateTurn turn, ParticipantModel participantModel) {
        return new DebateTurnView(
                turn.getId(),
                turn.getSessionId(),
                turn.getParticipantId(),
                participantModel,
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
    }
}
