package com.example.aichat.debate.application;

import com.example.aichat.debate.domain.DebateParticipant;
import com.example.aichat.debate.domain.ParticipantModel;

public record DebateParticipantView(
        Long id,
        Long sourceCharacterId,
        int position,
        ParticipantModel model,
        String name,
        String description,
        String personality,
        String speechStyle
) {

    public static DebateParticipantView from(DebateParticipant participant) {
        return new DebateParticipantView(
                participant.getId(),
                participant.getSourceCharacterId(),
                participant.getPosition(),
                participant.getModel(),
                participant.getName(),
                participant.getDescription(),
                participant.getPersonality(),
                participant.getSpeechStyle()
        );
    }
}
