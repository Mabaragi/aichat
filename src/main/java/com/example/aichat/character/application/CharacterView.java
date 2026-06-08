package com.example.aichat.character.application;

import com.example.aichat.character.domain.Character;

import java.time.LocalDateTime;

public record CharacterView(
        Long id,
        Long ownerId,
        String name,
        String description,
        String personality,
        String speechStyle,
        String visibility,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static CharacterView from(Character character) {
        return new CharacterView(
                character.getId(),
                character.getOwnerId(),
                character.getName(),
                character.getDescription(),
                character.getPersonality(),
                character.getSpeechStyle(),
                character.getVisibility(),
                character.getCreatedAt(),
                character.getUpdatedAt()
        );
    }
}
