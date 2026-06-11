package com.example.aichat.character.application;

import com.example.aichat.character.domain.Personality;
import com.example.aichat.character.domain.SpeechStyle;
import com.example.aichat.common.security.RequestActor;

public record CreateCharacterCommand(
        RequestActor actor,
        Long ownerId,
        String category,
        String name,
        String description,
        Personality personality,
        SpeechStyle speechStyle,
        String visibility
) {
    public CreateCharacterCommand(Long ownerId, String name, String description,
                                  Personality personality, SpeechStyle speechStyle,
                                  String visibility) {
        this(RequestActor.system(), ownerId, null, name, description,
                personality, speechStyle, visibility);
    }
}
