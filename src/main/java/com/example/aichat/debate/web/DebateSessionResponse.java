package com.example.aichat.debate.web;

import com.example.aichat.debate.application.DebateParticipantView;
import com.example.aichat.debate.application.DebateSessionView;
import com.example.aichat.debate.domain.DebateFormat;
import com.example.aichat.debate.domain.DebateSessionStatus;
import com.example.aichat.debate.domain.ParticipantModel;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

public record DebateSessionResponse(
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
        List<ParticipantResponse> participants,
        LocalDateTime createdAt
) {

    public static DebateSessionResponse from(DebateSessionView view, ObjectMapper objectMapper) {
        return new DebateSessionResponse(
                view.id(),
                view.ownerId(),
                view.topicTitle(),
                view.topicDescription(),
                view.topicCategory(),
                view.status(),
                view.format(),
                view.maxRounds(),
                view.currentRound(),
                view.maxTurnLength(),
                view.participants().stream()
                        .map(participant -> ParticipantResponse.from(participant, objectMapper))
                        .toList(),
                view.createdAt()
        );
    }

    public record ParticipantResponse(
            Long id,
            Long sourceCharacterId,
            int position,
            ParticipantModel model,
            String name,
            String description,
            JsonNode personality,
            JsonNode speechStyle
    ) {

        private static ParticipantResponse from(DebateParticipantView view,
                                                ObjectMapper objectMapper) {
            return new ParticipantResponse(
                    view.id(),
                    view.sourceCharacterId(),
                    view.position(),
                    view.model(),
                    view.name(),
                    view.description(),
                    toJsonNode(objectMapper, view.personality()),
                    toJsonNode(objectMapper, view.speechStyle())
            );
        }
    }

    private static JsonNode toJsonNode(ObjectMapper objectMapper, String rawJson) {
        if (rawJson == null) {
            return null;
        }

        try {
            return objectMapper.readTree(rawJson);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Failed to render debate participant snapshot", exception);
        }
    }
}
