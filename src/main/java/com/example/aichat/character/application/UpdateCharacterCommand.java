package com.example.aichat.character.application;

public record UpdateCharacterCommand(
        Long characterId,
        String name,
        String description,
        String personality,
        String speechStyle,
        String visibility
) {
}
