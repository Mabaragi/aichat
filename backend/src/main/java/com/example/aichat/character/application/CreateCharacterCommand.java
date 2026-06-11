package com.example.aichat.character.application;

import com.example.aichat.character.domain.Persona;
import com.example.aichat.common.security.RequestActor;

public record CreateCharacterCommand(
        RequestActor actor,
        Long ownerId,
        String category,
        String name,
        String description,
        Persona persona,
        String visibility
) {
    public CreateCharacterCommand(Long ownerId, String name, String description,
                                  Persona persona, String visibility) {
        this(RequestActor.system(), ownerId, null, name, description,
                persona, visibility);
    }
}
