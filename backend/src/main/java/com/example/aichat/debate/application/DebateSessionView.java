package com.example.aichat.debate.application;

import com.example.aichat.category.application.CategorySummaryView;
import com.example.aichat.debate.domain.DebateFormat;
import com.example.aichat.debate.domain.DebateSession;
import com.example.aichat.debate.domain.DebateSessionStatus;

import java.time.LocalDateTime;
import java.util.List;

public record DebateSessionView(
        Long id,
        Long ownerId,
        CategorySummaryView category,
        String topicTitle,
        String topicDescription,
        String topicCategory,
        String visibility,
        DebateSessionStatus status,
        DebateFormat format,
        int maxRounds,
        int currentRound,
        Integer maxTurnLength,
        List<DebateParticipantView> participants,
        LocalDateTime createdAt,
        LocalDateTime startedAt,
        LocalDateTime endedAt
) {

    public DebateSessionView(Long id, Long ownerId, String topicTitle,
                             String topicDescription, String topicCategory,
                             DebateSessionStatus status, DebateFormat format,
                             int maxRounds, int currentRound, Integer maxTurnLength,
                             List<DebateParticipantView> participants,
                             LocalDateTime createdAt) {
        this(id, ownerId, null, topicTitle, topicDescription, topicCategory,
                DebateSession.DEFAULT_VISIBILITY, status, format, maxRounds,
                currentRound, maxTurnLength, participants, createdAt, null, null);
    }

    public static DebateSessionView from(DebateSession session) {
        return from(session, null);
    }

    public static DebateSessionView from(DebateSession session, CategorySummaryView category) {
        return new DebateSessionView(
                session.getId(),
                session.getOwnerId(),
                category,
                session.getTopicTitle(),
                session.getTopicDescription(),
                session.getTopicCategory(),
                session.getVisibility(),
                session.getStatus(),
                session.getFormat(),
                session.getMaxRounds(),
                session.getCurrentRound(),
                session.getMaxTurnLength(),
                session.getParticipants().stream()
                        .map(DebateParticipantView::from)
                        .toList(),
                session.getCreatedAt(),
                session.getStartedAt(),
                session.getEndedAt()
        );
    }
}
