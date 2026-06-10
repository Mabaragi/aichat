package com.example.aichat.debate.application;

import com.example.aichat.debate.domain.ParticipantModel;

public record CreateDebateParticipantCommand(
        Long characterId,
        ParticipantModel model
) {
}
