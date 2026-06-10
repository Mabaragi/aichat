package com.example.aichat.debate.application;

import com.example.aichat.common.exception.BusinessException;
import com.example.aichat.common.exception.ErrorCode;
import com.example.aichat.common.security.RequestActor;
import com.example.aichat.common.time.TimeProvider;
import com.example.aichat.debate.domain.DebateSession;
import com.example.aichat.debate.domain.DebateSessionRepository;
import com.example.aichat.debate.domain.DebateSessionStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompleteDebateSessionUseCase {

    private final DebateSessionRepository debateSessionRepository;
    private final TimeProvider timeProvider;

    public CompleteDebateSessionUseCase(DebateSessionRepository debateSessionRepository,
                                        TimeProvider timeProvider) {
        this.debateSessionRepository = debateSessionRepository;
        this.timeProvider = timeProvider;
    }

    @Transactional
    public DebateSessionLifecycleView execute(CompleteDebateSessionCommand command) {
        DebateSession session = requireSession(command.actor(), command.sessionId());
        if (session.getStatus() != DebateSessionStatus.RUNNING) {
            throw new BusinessException(
                    ErrorCode.INVALID_SESSION_STATE,
                    "session must be RUNNING to complete"
            );
        }

        session.complete(timeProvider.now());
        return DebateSessionLifecycleView.from(debateSessionRepository.save(session));
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
}
