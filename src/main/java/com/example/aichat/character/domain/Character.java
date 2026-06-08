package com.example.aichat.character.domain;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Locale;

@Getter
public class Character {

    public static final String DEFAULT_VISIBILITY = "PRIVATE";

    private final Long id;
    private final Long ownerId;
    private String name;
    private String description;
    private String personality;
    private String speechStyle;
    private String visibility;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Character(Long id, Long ownerId, String name, String description,
                     String personality, String speechStyle, String visibility,
                     LocalDateTime createdAt, LocalDateTime updatedAt) {
        validateOwnerId(ownerId);
        validateName(name);
        validateDescription(description);
        validateTimestamp("createdAt", createdAt);
        validateTimestamp("updatedAt", updatedAt);

        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.description = description;
        this.personality = personality;
        this.speechStyle = speechStyle;
        this.visibility = normalizeVisibility(visibility);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Character create(Long ownerId, String name,
                                   String description, String personality,
                                   String speechStyle, LocalDateTime createdAt,
                                   LocalDateTime updatedAt) {
        return create(ownerId, name, description, personality, speechStyle,
                DEFAULT_VISIBILITY, createdAt, updatedAt);
    }

    public static Character create(Long ownerId, String name,
                                   String description, String personality,
                                   String speechStyle, String visibility,
                                   LocalDateTime createdAt,
                                   LocalDateTime updatedAt) {
        return new Character(null, ownerId, name, description, personality,
                speechStyle, visibility, createdAt, updatedAt);
    }

    public void update(String name, String description, String personality,
                       String speechStyle, String visibility,
                       LocalDateTime updatedAt) {
        validateName(name);
        validateDescription(description);
        validateTimestamp("updatedAt", updatedAt);

        this.name = name;
        this.description = description;
        this.personality = personality;
        this.speechStyle = speechStyle;
        this.visibility = normalizeVisibility(visibility);
        this.updatedAt = updatedAt;
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
        if (visibility == null || visibility.isBlank()) {
            return DEFAULT_VISIBILITY;
        }

        return visibility.trim().toUpperCase(Locale.ROOT);
    }
}
