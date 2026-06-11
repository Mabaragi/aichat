package com.example.aichat.character.web;

import com.example.aichat.character.application.CharacterView;
import com.example.aichat.category.web.CategorySummaryResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import tools.jackson.core.type.TypeReference;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Character response.")
public record CharacterResponse(
        @Schema(description = "Character identifier.", example = "1")
        Long id,
        @Schema(description = "Owner user identifier.", example = "1")
        Long ownerId,
        @Schema(description = "Character category summary.")
        CategorySummaryResponse category,
        @Schema(description = "Character name.", example = "Rational Gourmet")
        String name,
        @Schema(description = "Short character description.",
                example = "A calm debater who analyzes food choices logically.")
        String description,
        @Schema(description = "Personality traits object.",
                example = "{\"rationality\":90,\"humor\":30}")
        Map<String, Object> personality,
        @Schema(description = "Speech style object.",
                example = "{\"tone\":\"calm\",\"formality\":\"high\"}")
        Map<String, Object> speechStyle,
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
                CategorySummaryResponse.from(view.category()),
                view.name(),
                view.description(),
                toJsonObject(objectMapper, view.personality()),
                toJsonObject(objectMapper, view.speechStyle()),
                view.visibility(),
                view.createdAt(),
                view.updatedAt()
        );
    }

    private static Map<String, Object> toJsonObject(ObjectMapper objectMapper, String rawJson) {
        if (rawJson == null) {
            return null;
        }

        try {
            return objectMapper.readValue(rawJson, new TypeReference<>() {
            });
        } catch (JacksonException exception) {
            throw new IllegalStateException("Failed to render character response", exception);
        }
    }
}
