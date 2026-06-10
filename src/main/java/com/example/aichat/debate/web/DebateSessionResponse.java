package com.example.aichat.debate.web;

import com.example.aichat.debate.application.DebateParticipantView;
import com.example.aichat.debate.application.DebateSessionView;
import com.example.aichat.debate.domain.DebateFormat;
import com.example.aichat.debate.domain.DebateSessionStatus;
import com.example.aichat.debate.domain.ParticipantModel;
import io.swagger.v3.oas.annotations.media.Schema;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Debate session response.")
public record DebateSessionResponse(
        @Schema(description = "Debate session identifier.", example = "1")
        Long id,
        @Schema(description = "Owner user identifier.", example = "1")
        Long ownerId,
        @Schema(description = "Topic title.", example = "Sauce-first vs dip-first")
        String topicTitle,
        @Schema(description = "Topic description.",
                example = "Which serving style creates the better eating experience?")
        String topicDescription,
        @Schema(description = "Optional topic category.", example = "FOOD")
        String topicCategory,
        @Schema(description = "Current session status.", example = "CREATED")
        DebateSessionStatus status,
        @Schema(description = "Configured debate format.", example = "PROS_AND_CONS")
        DebateFormat format,
        @Schema(description = "Maximum round count.", example = "5")
        int maxRounds,
        @Schema(description = "Current completed round count.", example = "0")
        int currentRound,
        @Schema(description = "Maximum length of each turn in characters.", example = "600")
        Integer maxTurnLength,
        @Schema(description = "Two participant snapshots captured at session creation.")
        List<ParticipantResponse> participants,
        @Schema(description = "Timestamp when the session was created.",
                example = "2026-06-10T12:00:00")
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

    @Schema(name = "DebateSessionParticipantResponse",
            description = "Participant snapshot captured when the session was created.")
    public record ParticipantResponse(
            @Schema(description = "Participant identifier.", example = "1")
            Long id,
            @Schema(description = "Source character identifier.", example = "10")
            Long sourceCharacterId,
            @Schema(description = "Zero-based participant position.", example = "0")
            int position,
            @Schema(description = "Generation model assigned to this participant.",
                    example = "FAST")
            ParticipantModel model,
            @Schema(description = "Participant display name.", example = "Rational Gourmet")
            String name,
            @Schema(description = "Participant description.",
                    example = "A calm debater who analyzes food choices logically.")
            String description,
            @Schema(description = "Participant personality snapshot.",
                    type = "object",
                    example = "{\"rationality\":90}")
            JsonNode personality,
            @Schema(description = "Participant speech style snapshot.",
                    type = "object",
                    example = "{\"tone\":\"calm\"}")
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
