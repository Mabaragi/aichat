package com.example.aichat.debate.application;

import com.example.aichat.category.application.CategoryResolver;
import com.example.aichat.category.application.CategorySummaryView;
import com.example.aichat.category.domain.Category;
import com.example.aichat.category.domain.CategoryScope;
import com.example.aichat.common.application.PagedResult;
import com.example.aichat.debate.domain.DebateSession;
import com.example.aichat.debate.domain.DebateSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;

@Service
public class ListPublicDebateSessionsUseCase {

    private final DebateSessionRepository debateSessionRepository;
    private final CategoryResolver categoryResolver;

    public ListPublicDebateSessionsUseCase(DebateSessionRepository debateSessionRepository,
                                           CategoryResolver categoryResolver) {
        this.debateSessionRepository = debateSessionRepository;
        this.categoryResolver = categoryResolver;
    }

    @Transactional(readOnly = true)
    public PagedResult<DebateSessionView> execute(String query, String categorySlug,
                                                  int page, int size) {
        Category category = categorySlug == null || categorySlug.isBlank()
                ? null
                : categoryResolver.requireActive(CategoryScope.DEBATE, categorySlug);
        PagedResult<DebateSession> result = debateSessionRepository.findPublicCompleted(
                query,
                category == null ? null : category.getId(),
                page,
                size
        );
        Map<Long, CategorySummaryView> categories = categoryResolver.summariesById(
                result.items().stream()
                        .map(DebateSession::getCategoryId)
                        .filter(Objects::nonNull)
                        .toList()
        );
        return new PagedResult<>(
                result.items().stream()
                        .map(session -> DebateSessionView.from(
                                session,
                                categories.get(session.getCategoryId())
                        ))
                        .toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext()
        );
    }
}
