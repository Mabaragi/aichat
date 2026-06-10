package com.example.aichat.debate.application;

import com.example.aichat.common.security.RequestActor;

public record GenerateDebateTurnCommand(
        RequestActor actor,
        Long sessionId
) {
}
