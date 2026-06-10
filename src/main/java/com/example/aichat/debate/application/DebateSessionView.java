package com.example.aichat.debate.application;

import com.example.aichat.debate.domain.DebateFormat;
import com.example.aichat.debate.domain.DebateSession;
import com.example.aichat.debate.domain.DebateSessionStatus;

import java.time.LocalDateTime;
import java.util.List;

public record DebateSessionView(
        Long id,
        Long ownerId,
        String topicTitle,
        String topicDescription,
        String topicCategory,
        DebateSessionStatus status,
        DebateFormat format,
        int maxRounds,
        int currentRound,
        Integer maxTurnLength,
        List<DebateParticipantView> participants,
        LocalDateTime createdAt
) {

    public static DebateSessionView from(DebateSession session) {
        return new DebateSessionView(
                session.getId(),
                session.getOwnerId(),
                session.getTopicTitle(),
                session.getTopicDescription(),
                session.getTopicCategory(),
                session.getStatus(),
                session.getFormat(),
                session.getMaxRounds(),
                session.getCurrentRound(),
                session.getMaxTurnLength(),
                session.getParticipants().stream()
                        .map(DebateParticipantView::from)
                        .toList(),
                session.getCreatedAt()
        );
    }
}
