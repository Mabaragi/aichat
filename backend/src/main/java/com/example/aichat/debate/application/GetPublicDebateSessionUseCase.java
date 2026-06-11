package com.example.aichat.debate.application;

import com.example.aichat.category.application.CategoryResolver;
import com.example.aichat.category.application.CategorySummaryView;
import com.example.aichat.common.exception.BusinessException;
import com.example.aichat.common.exception.ErrorCode;
import com.example.aichat.debate.domain.DebateSession;
import com.example.aichat.debate.domain.DebateSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GetPublicDebateSessionUseCase {

    private final DebateSessionRepository debateSessionRepository;
    private final CategoryResolver categoryResolver;

    public GetPublicDebateSessionUseCase(DebateSessionRepository debateSessionRepository,
                                         CategoryResolver categoryResolver) {
        this.debateSessionRepository = debateSessionRepository;
        this.categoryResolver = categoryResolver;
    }

    @Transactional(readOnly = true)
    public DebateSessionView execute(Long sessionId) {
        DebateSession session = debateSessionRepository.findPublicCompletedById(sessionId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DEBATE_SESSION_NOT_FOUND,
                        "Debate session not found: " + sessionId
                ));
        CategorySummaryView category = session.getCategoryId() == null
                ? null
                : categoryResolver.summariesById(List.of(session.getCategoryId()))
                .get(session.getCategoryId());
        return DebateSessionView.from(session, category);
    }
}
