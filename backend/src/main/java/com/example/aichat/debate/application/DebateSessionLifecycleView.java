package com.example.aichat.debate.application;

import com.example.aichat.debate.domain.DebateSession;
import com.example.aichat.debate.domain.DebateSessionStatus;

import java.time.LocalDateTime;

public record DebateSessionLifecycleView(
        Long id,
        DebateSessionStatus status,
        LocalDateTime startedAt,
        LocalDateTime endedAt
) {

    public static DebateSessionLifecycleView from(DebateSession session) {
        return new DebateSessionLifecycleView(
                session.getId(),
                session.getStatus(),
                session.getStartedAt(),
                session.getEndedAt()
        );
    }
}
