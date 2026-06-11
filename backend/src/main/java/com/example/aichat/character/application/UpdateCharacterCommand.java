package com.example.aichat.character.application;

import com.example.aichat.character.domain.Persona;
import com.example.aichat.common.security.RequestActor;

public record UpdateCharacterCommand(
        RequestActor actor,
        Long characterId,
        String category,
        String name,
        String description,
        Persona persona,
        String visibility
) {
    public UpdateCharacterCommand(RequestActor actor, Long characterId, String name,
                                  String description, Persona persona, String visibility) {
        this(actor, characterId, null, name, description, persona, visibility);
    }

    public UpdateCharacterCommand(Long characterId, String name, String description,
                                  Persona persona, String visibility) {
        this(RequestActor.system(), characterId, null, name, description,
                persona, visibility);
    }
}
