package com.example.aichat.character.application;

import com.example.aichat.character.domain.Personality;
import com.example.aichat.character.domain.SpeechStyle;

public record CreateCharacterCommand(
        Long ownerId,
        String name,
        String description,
        Personality personality,
        SpeechStyle speechStyle,
        String visibility
) {
}
