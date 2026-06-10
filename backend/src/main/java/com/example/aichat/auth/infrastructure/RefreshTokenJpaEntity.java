package com.example.aichat.auth.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
class RefreshTokenJpaEntity {

    protected RefreshTokenJpaEntity() {
    }

    RefreshTokenJpaEntity(Long id, Long userId, UUID familyId, String sessionId,
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "user_id", nullable = false)
    Long userId;

    @Column(name = "family_id", nullable = false)
    UUID familyId;

    @Column(name = "session_id", nullable = false)
    String sessionId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    String tokenHash;

    @Column(name = "expires_at", nullable = false)
    LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    LocalDateTime revokedAt;

    @Column(name = "replaced_by_hash", length = 64)
    String replacedByHash;

    @Version
    long version;
}
