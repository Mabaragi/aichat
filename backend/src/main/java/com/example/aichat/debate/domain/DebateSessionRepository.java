package com.example.aichat.debate.domain;

import java.util.Optional;

public interface DebateSessionRepository {

    DebateSession save(DebateSession session);

    Optional<DebateSession> findById(Long sessionId);
}
