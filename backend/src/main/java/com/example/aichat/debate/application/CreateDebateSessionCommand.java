package com.example.aichat.debate.application;

import com.example.aichat.debate.domain.DebateFormat;
import com.example.aichat.common.security.RequestActor;

import java.util.List;

public record CreateDebateSessionCommand(
        RequestActor actor,
        Long ownerId,
        String topicTitle,
        String topicDescription,
        String topicCategory,
        String visibility,
        DebateFormat format,
        int maxRounds,
        Integer maxTurnLength,
        List<CreateDebateParticipantCommand> participants
) {
    public CreateDebateSessionCommand(Long ownerId, String topicTitle,
                                      String topicDescription, String topicCategory,
                                      DebateFormat format, int maxRounds,
                                      Integer maxTurnLength,
                                      List<CreateDebateParticipantCommand> participants) {
        this(RequestActor.system(), ownerId, topicTitle, topicDescription,
                topicCategory, null, format, maxRounds, maxTurnLength, participants);
    }
}
