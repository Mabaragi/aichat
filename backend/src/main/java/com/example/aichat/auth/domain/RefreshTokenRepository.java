package com.example.aichat.auth.domain;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    void revokeFamily(UUID familyId, LocalDateTime revokedAt);
}
