package com.example.aichat.debate.web;

import com.example.aichat.debate.application.DebateTurnView;
import com.example.aichat.debate.domain.ParticipantModel;
import com.example.aichat.debate.domain.TurnType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Debate turn summary response.")
public record DebateTurnResponse(
        @Schema(description = "Debate turn identifier.", example = "1")
        Long id,
        @Schema(description = "Debate participant identifier.", example = "1")
        Long participantId,
        @Schema(description = "Participant model used for the turn.", example = "FAST")
        ParticipantModel participantModel,
        @Schema(description = "Round number.", example = "1")
        int round,
        @Schema(description = "Turn index within the session.", example = "1")
        int turnIndex,
        @Schema(description = "Turn type.", example = "ARGUMENT")
        TurnType type,
        @Schema(description = "Generated content.",
                example = "부먹은 소스와 튀김의 조화를 극대화합니다.")
        String content,
        @Schema(description = "Timestamp when the turn was created.",
                example = "2026-06-05T12:02:00")
        LocalDateTime createdAt
) {

    public static DebateTurnResponse from(DebateTurnView view) {
        return new DebateTurnResponse(
                view.id(),
                view.participantId(),
                view.participantModel(),
                view.round(),
                view.turnIndex(),
                view.type(),
                view.content(),
                view.createdAt()
        );
    }
}
