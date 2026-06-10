package com.example.aichat.character.web;

import com.example.aichat.character.application.CharacterView;
import io.swagger.v3.oas.annotations.media.Schema;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Schema(description = "Character response.")
public record CharacterResponse(
        @Schema(description = "Character identifier.", example = "1")
        Long id,
        @Schema(description = "Owner user identifier.", example = "1")
        Long ownerId,
        @Schema(description = "Character name.", example = "Rational Gourmet")
        String name,
        @Schema(description = "Short character description.",
                example = "A calm debater who analyzes food choices logically.")
        String description,
        @Schema(description = "Personality traits object.",
                type = "object",
                example = "{\"rationality\":90,\"humor\":30}")
        JsonNode personality,
        @Schema(description = "Speech style object.",
                type = "object",
                example = "{\"tone\":\"calm\",\"formality\":\"high\"}")
        JsonNode speechStyle,
        @Schema(description = "Character visibility.",
                allowableValues = {"PUBLIC", "PRIVATE"},
                example = "PRIVATE")
        String visibility,
        @Schema(description = "Timestamp when the character was created.",
                example = "2026-06-08T12:00:00")
        LocalDateTime createdAt,
        @Schema(description = "Timestamp when the character was last updated.",
                example = "2026-06-08T12:30:00")
        LocalDateTime updatedAt
) {

    public static CharacterResponse from(CharacterView view, ObjectMapper objectMapper) {
        return new CharacterResponse(
                view.id(),
                view.ownerId(),
                view.name(),
                view.description(),
                toJsonNode(objectMapper, view.personality()),
                toJsonNode(objectMapper, view.speechStyle()),
                view.visibility(),
                view.createdAt(),
                view.updatedAt()
        );
    }

    private static JsonNode toJsonNode(ObjectMapper objectMapper, String rawJson) {
        if (rawJson == null) {
            return null;
        }

        try {
            return objectMapper.readTree(rawJson);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Failed to render character response", exception);
        }
    }
}
