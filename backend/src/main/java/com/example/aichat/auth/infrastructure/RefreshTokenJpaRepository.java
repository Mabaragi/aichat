package com.example.aichat.auth.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, Long> {

    Optional<RefreshTokenJpaEntity> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
            update RefreshTokenJpaEntity token
               set token.revokedAt = :revokedAt
             where token.familyId = :familyId
               and token.revokedAt is null
            """)
    void revokeFamily(UUID familyId, LocalDateTime revokedAt);
}
