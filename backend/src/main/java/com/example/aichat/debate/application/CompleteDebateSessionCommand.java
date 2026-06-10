package com.example.aichat.debate.application;

import com.example.aichat.common.security.RequestActor;

public record CompleteDebateSessionCommand(
        RequestActor actor,
        Long sessionId
) {
}
