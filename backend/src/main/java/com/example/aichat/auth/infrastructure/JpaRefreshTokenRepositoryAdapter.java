package com.example.aichat.auth.infrastructure;

import com.example.aichat.auth.domain.RefreshToken;
import com.example.aichat.auth.domain.RefreshTokenRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaRefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository repository;

    public JpaRefreshTokenRepositoryAdapter(RefreshTokenJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        return toDomain(repository.save(toEntity(token)));
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash).map(JpaRefreshTokenRepositoryAdapter::toDomain);
    }

    @Override
    public void revokeFamily(UUID familyId, LocalDateTime revokedAt) {
        repository.revokeFamily(familyId, revokedAt);
    }

    private static RefreshTokenJpaEntity toEntity(RefreshToken token) {
        return new RefreshTokenJpaEntity(
                token.getId(), token.getUserId(), token.getFamilyId(), token.getSessionId(),
                token.getTokenHash(), token.getExpiresAt(), token.getRevokedAt(),
                token.getReplacedByHash(), token.getVersion()
        );
    }

    private static RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        return new RefreshToken(
                entity.id, entity.userId, entity.familyId, entity.sessionId,
                entity.tokenHash, entity.expiresAt, entity.revokedAt,
                entity.replacedByHash, entity.version
        );
    }
}
