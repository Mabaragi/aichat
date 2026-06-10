package com.example.aichat.character.application;

import com.example.aichat.character.domain.Personality;
import com.example.aichat.character.domain.SpeechStyle;
import com.example.aichat.common.security.RequestActor;

public record UpdateCharacterCommand(
        RequestActor actor,
        Long characterId,
        String name,
        String description,
        Personality personality,
        SpeechStyle speechStyle,
        String visibility
) {
    public UpdateCharacterCommand(Long characterId, String name, String description,
                                  Personality personality, SpeechStyle speechStyle,
                                  String visibility) {
        this(RequestActor.system(), characterId, name, description,
                personality, speechStyle, visibility);
    }
}
