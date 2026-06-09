package com.example.aichat.character.web;

import com.example.aichat.character.application.CharacterView;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

public record CharacterResponse(
        Long id,
        Long ownerId,
        String name,
        String description,
        JsonNode personality,
        JsonNode speechStyle,
        String visibility,
        LocalDateTime createdAt,
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
