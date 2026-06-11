package com.example.aichat.character.application;

import com.example.aichat.category.application.CategorySummaryView;
import com.example.aichat.character.domain.Character;
import com.example.aichat.character.domain.Persona;

import java.time.LocalDateTime;

public record CharacterView(
        Long id,
        Long ownerId,
        CategorySummaryView category,
        String name,
        String description,
        String persona,
        String visibility,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public CharacterView(Long id, Long ownerId, String name, String description,
                         String persona, String visibility,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(id, ownerId, null, name, description, persona, visibility, createdAt, updatedAt);
    }

    public static CharacterView from(Character character) {
        return from(character, null);
    }

    public static CharacterView from(Character character, CategorySummaryView category) {
        return new CharacterView(
                character.getId(),
                character.getOwnerId(),
                category,
                character.getName(),
                character.getDescription(),
                unwrap(character.getPersona()),
                character.getVisibility(),
                character.getCreatedAt(),
                character.getUpdatedAt()
        );
    }

    private static String unwrap(Persona persona) {
        return persona == null ? null : persona.value();
    }
}
