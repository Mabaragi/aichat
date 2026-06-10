package com.example.aichat.auth.domain;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class RefreshToken {

    private final Long id;
    private final Long userId;
    private final UUID familyId;
    private final String sessionId;
    private final String tokenHash;
    private final LocalDateTime expiresAt;
    private LocalDateTime revokedAt;
    private String replacedByHash;
    private final long version;

    public RefreshToken(Long id, Long userId, UUID familyId, String sessionId,
                        String tokenHash, LocalDateTime expiresAt, LocalDateTime revokedAt,
                        String replacedByHash, long version) {
        this.id = id;
        this.userId = userId;
        this.familyId = familyId;
        this.sessionId = sessionId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
        this.replacedByHash = replacedByHash;
        this.version = version;
    }

    public static RefreshToken issue(Long userId, UUID familyId, String sessionId,
                                     String tokenHash, LocalDateTime expiresAt) {
        return new RefreshToken(null, userId, familyId, sessionId, tokenHash,
                expiresAt, null, null, 0);
    }

    public boolean isActive(LocalDateTime now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }

    public void rotate(String replacementHash, LocalDateTime now) {
        this.revokedAt = now;
        this.replacedByHash = replacementHash;
    }

    public void revoke(LocalDateTime now) {
        if (revokedAt == null) {
            revokedAt = now;
        }
    }
}
