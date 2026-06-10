package com.example.aichat.debate.web;

import com.example.aichat.debate.application.DebateTurnView;
import com.example.aichat.debate.domain.TurnStatus;
import com.example.aichat.debate.domain.TurnType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Generated debate turn response.")
public record GenerateTurnResponse(
        @Schema(description = "Debate turn identifier.", example = "1")
        Long id,
        @Schema(description = "Debate session identifier.", example = "1")
        Long sessionId,
        @Schema(description = "Debate participant identifier.", example = "1")
        Long participantId,
        @Schema(description = "Round number.", example = "1")
        int round,
        @Schema(description = "Turn index within the session.", example = "1")
        int turnIndex,
        @Schema(description = "Turn type.", example = "ARGUMENT")
        TurnType type,
        @Schema(description = "Turn generation status.", example = "COMPLETED")
        TurnStatus status,
        @Schema(description = "Generated content.",
                example = "부먹은 소스와 튀김의 조화를 극대화합니다.")
        String content,
        @Schema(description = "Actual model name used for generation.", example = "mock-model")
        String modelName,
        @Schema(description = "Input token count.", example = "0")
        Integer inputTokens,
        @Schema(description = "Output token count.", example = "0")
        Integer outputTokens,
        @Schema(description = "Timestamp when the turn was created.",
                example = "2026-06-05T12:02:00")
        LocalDateTime createdAt
) {

    public static GenerateTurnResponse from(DebateTurnView view) {
        return new GenerateTurnResponse(
                view.id(),
                view.sessionId(),
                view.participantId(),
                view.round(),
                view.turnIndex(),
                view.type(),
                view.status(),
                view.content(),
                view.modelName(),
                view.inputTokens(),
                view.outputTokens(),
                view.createdAt()
        );
    }
}
