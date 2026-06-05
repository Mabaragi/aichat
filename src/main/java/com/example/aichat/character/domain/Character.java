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
    private String personality;
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
            String personality,
            LocalDateTime createdAt,
            LocalDateTime updatedAt

    ) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.description = description;
        this.personality = personality;
        this.speechStyle = speechStyle;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    public static Character create(
            Long ownerId,
            String name,
            String description,
            String personality,
            String speechStyle,
            LocalDateTime createAt,
            LocalDateTime updatedAt
    ) {

        Objects.requireNonNull(ownerId, "ownerId cannot be null");
        validateName(name);
        validateDescription(description);

        return new Character(null, ownerId, name, description, speechStyle, personality, createAt, updatedAt);
    }

    public void update(
            String name,
            String description,
            String personality,
            String speechStyle,
            LocalDateTime updatedAt
    ){
        validateName(name);
        validateDescription(description);

        this.name = name;
        this.description = description;
        this.personality = personality;
        this.speechStyle = speechStyle;
        this.updatedAt = updatedAt;
    }

    private static void validateName(String name){
        Objects.requireNonNull(name, "name cannot be null");
        if (name.isBlank()){
            throw new IllegalArgumentException("name cannot be blank");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("name too long");
        }
    }
    private static void validateDescription(String description){
        if  (description != null && description.length() > 1000) {
            throw new IllegalArgumentException("description too long");
        }
    }
}
