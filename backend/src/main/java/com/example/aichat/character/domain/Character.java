package com.example.aichat.character.domain;

import lombok.Getter;

import java.time.LocalDateTime;
import com.example.aichat.common.domain.Visibility;

@Getter
public class Character {

    public static final String DEFAULT_VISIBILITY = Visibility.DEFAULT;

    private final Long id;
    private final Long ownerId;
    private Long categoryId;
    private String name;
    private String description;
    private Personality personality;
    private SpeechStyle speechStyle;
    private String visibility;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Character(Long id, Long ownerId, String name, String description,
                     Personality personality, SpeechStyle speechStyle, String visibility,
                     LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(id, ownerId, null, name, description, personality, speechStyle,
                visibility, createdAt, updatedAt);
    }

    public Character(Long id, Long ownerId, Long categoryId, String name, String description,
                     Personality personality, SpeechStyle speechStyle, String visibility,
                     LocalDateTime createdAt, LocalDateTime updatedAt) {
        validateOwnerId(ownerId);
        validateName(name);
        validateDescription(description);
        validateTimestamp("createdAt", createdAt);
        validateTimestamp("updatedAt", updatedAt);

        this.id = id;
        this.ownerId = ownerId;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.personality = personality;
        this.speechStyle = speechStyle;
        this.visibility = normalizeVisibility(visibility);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Character create(Long ownerId, String name,
                                   String description, Personality personality,
                                   SpeechStyle speechStyle, LocalDateTime createdAt,
                                   LocalDateTime updatedAt) {
        return create(ownerId, null, name, description, personality, speechStyle,
                createdAt, updatedAt);
    }

    public static Character create(Long ownerId, Long categoryId, String name,
                                   String description, Personality personality,
                                   SpeechStyle speechStyle, LocalDateTime createdAt,
                                   LocalDateTime updatedAt) {
        return create(ownerId, categoryId, name, description, personality, speechStyle,
                DEFAULT_VISIBILITY, createdAt, updatedAt);
    }

    public static Character create(Long ownerId, String name,
                                   String description, Personality personality,
                                   SpeechStyle speechStyle, String visibility,
                                   LocalDateTime createdAt,
                                   LocalDateTime updatedAt) {
        return create(ownerId, null, name, description, personality, speechStyle,
                visibility, createdAt, updatedAt);
    }

    public static Character create(Long ownerId, Long categoryId, String name,
                                   String description, Personality personality,
                                   SpeechStyle speechStyle, String visibility,
                                   LocalDateTime createdAt,
                                   LocalDateTime updatedAt) {
        return new Character(null, ownerId, categoryId, name, description, personality,
                speechStyle, visibility, createdAt, updatedAt);
    }

    public void update(Long categoryId, String name, String description, Personality personality,
                       SpeechStyle speechStyle, String visibility,
                       LocalDateTime updatedAt) {
        validateName(name);
        validateDescription(description);
        validateTimestamp("updatedAt", updatedAt);

        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.personality = personality;
        this.speechStyle = speechStyle;
        this.visibility = normalizeVisibility(visibility);
        this.updatedAt = updatedAt;
    }

    public void update(String name, String description, Personality personality,
                       SpeechStyle speechStyle, String visibility,
                       LocalDateTime updatedAt) {
        update(categoryId, name, description, personality, speechStyle, visibility, updatedAt);
    }

    private static void validateOwnerId(Long ownerId) {
        if (ownerId == null) {
            throw new IllegalArgumentException("ownerId is required");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }

        if (name.length() > 50) {
            throw new IllegalArgumentException("name too long");
        }
    }

    private static void validateDescription(String description) {
        if (description != null && description.length() > 1000) {
            throw new IllegalArgumentException("description too long");
        }
    }

    private static void validateTimestamp(String fieldName,
                                          LocalDateTime timestamp) {
        if (timestamp == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }

    private static String normalizeVisibility(String visibility) {
        return Visibility.normalize(visibility);
    }
}
