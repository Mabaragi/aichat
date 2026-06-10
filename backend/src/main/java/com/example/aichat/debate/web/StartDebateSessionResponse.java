package com.example.aichat.debate.web;

import com.example.aichat.debate.application.DebateSessionLifecycleView;
import com.example.aichat.debate.domain.DebateSessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Debate session start response.")
public record StartDebateSessionResponse(
        @Schema(description = "Debate session identifier.", example = "1")
        Long id,
        @Schema(description = "Current session status.", example = "RUNNING")
        DebateSessionStatus status,
        @Schema(description = "Timestamp when the session was started.",
                example = "2026-06-05T12:01:00")
        LocalDateTime startedAt
) {

    public static StartDebateSessionResponse from(DebateSessionLifecycleView view) {
        return new StartDebateSessionResponse(
                view.id(),
                view.status(),
                view.startedAt()
        );
    }
}
