package com.example.aichat.character.web;

import com.fasterxml.jackson.databind.JsonNode;

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
}
