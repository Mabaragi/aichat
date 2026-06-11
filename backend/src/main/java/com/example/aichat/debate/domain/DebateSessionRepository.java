package com.example.aichat.debate.domain;

import com.example.aichat.common.application.PagedResult;

import java.util.Optional;

public interface DebateSessionRepository {

    DebateSession save(DebateSession session);

    Optional<DebateSession> findById(Long sessionId);

    default PagedResult<DebateSession> findPublicCompleted(String query, Long categoryId,
                                                           int page, int size) {
        return new PagedResult<>(java.util.List.of(), page, size, 0, 0, false);
    }

    default Optional<DebateSession> findPublicCompletedById(Long sessionId) {
        return Optional.empty();
    }
}
