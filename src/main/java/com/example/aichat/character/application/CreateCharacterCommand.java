package com.example.aichat.character.application;

public record CreateCharacterCommand(
        Long ownerId,
        String name,
        String description,
        String personality,
        String speechStyle,
        String visibility
) {
}
