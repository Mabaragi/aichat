package com.example.aichat.auth.application;

import java.time.Instant;
import java.util.UUID;

public record IssuedToken(
        String value,
        String tokenHash,
        UUID familyId,
        String sessionId,
        Instant expiresAt
) {
}
