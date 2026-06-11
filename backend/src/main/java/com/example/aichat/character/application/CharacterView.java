package com.example.aichat.character.application;

import com.example.aichat.category.application.CategorySummaryView;
import com.example.aichat.character.domain.Character;
import com.example.aichat.character.domain.Personality;
import com.example.aichat.character.domain.SpeechStyle;

import java.time.LocalDateTime;

public record CharacterView(
        Long id,
        Long ownerId,
        CategorySummaryView category,
        String name,
        String description,
        String personality,
        String speechStyle,
        String visibility,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public CharacterView(Long id, Long ownerId, String name, String description,
                         String personality, String speechStyle, String visibility,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(id, ownerId, null, name, description, personality, speechStyle,
                visibility, createdAt, updatedAt);
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
                unwrap(character.getPersonality()),
                unwrap(character.getSpeechStyle()),
                character.getVisibility(),
                character.getCreatedAt(),
                character.getUpdatedAt()
        );
    }

    private static String unwrap(Personality personality) {
        return personality == null ? null : personality.value();
    }

    private static String unwrap(SpeechStyle speechStyle) {
        return speechStyle == null ? null : speechStyle.value();
    }
}
