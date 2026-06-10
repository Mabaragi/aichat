package com.example.aichat.debate.web;

import com.example.aichat.debate.application.DebateSessionLifecycleView;
import com.example.aichat.debate.domain.DebateSessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Debate session completion response.")
public record CompleteDebateSessionResponse(
        @Schema(description = "Debate session identifier.", example = "1")
        Long id,
        @Schema(description = "Current session status.", example = "COMPLETED")
        DebateSessionStatus status,
        @Schema(description = "Timestamp when the session was completed.",
                example = "2026-06-05T12:10:00")
        LocalDateTime endedAt
) {

    public static CompleteDebateSessionResponse from(DebateSessionLifecycleView view) {
        return new CompleteDebateSessionResponse(
                view.id(),
                view.status(),
                view.endedAt()
        );
    }
}
