package com.example.aichat.character.domain;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class Character {

    private final Long id;
    private final Long ownerId;
    private String name;
    private String description;
    private String speechStyle;
    private final String visibility = "PRIVATE";
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Character(
            Long id,
            Long ownerId,
            String name,
            String description,
            String speechStyle,
            LocalDateTime createdAt,
            LocalDateTime updatedAt

    ) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.description = description;
        this.speechStyle = speechStyle;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    public static Character create(
            Long ownerId,
            String name,
            String description,
            String personality,
            String speachStyle,
            LocalDateTime createAt,
            LocalDateTime updatedAt
    ) {

        Objects.requireNonNull(ownerId, "ownerId cannot be null");
        Objects.requireNonNull(name, "name cannot be null");

        if (name.length() > 50) {
            throw new IllegalArgumentException("name too long");
        }

        if (description.length() > 1000) {
            throw new IllegalArgumentException("description too long");
        }

        return new Character(null, ownerId, name, description, speachStyle, createAt, updatedAt);
    }
}
