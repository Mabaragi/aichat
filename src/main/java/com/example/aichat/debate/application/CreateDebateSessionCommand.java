package com.example.aichat.debate.application;

import com.example.aichat.debate.domain.DebateFormat;

import java.util.List;

public record CreateDebateSessionCommand(
        Long ownerId,
        String topicTitle,
        String topicDescription,
        String topicCategory,
        DebateFormat format,
        int maxRounds,
        Integer maxTurnLength,
        List<CreateDebateParticipantCommand> participants
) {
}
