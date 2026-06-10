package com.example.aichat.user.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class UserJpaEntity {

    protected UserJpaEntity() {
    }

    UserJpaEntity(Long id, String email, String passwordHash, String nickname, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.createdAt = createdAt;
    }

    Long id() {
        return id;
    }

    String email() {
        return email;
    }

    String nickname() {
        return nickname;
    }

    String passwordHash() {
        return passwordHash;
    }

    LocalDateTime createdAt() {
        return createdAt;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false,
            columnDefinition = "TEXT NOT NULL DEFAULT ''")
    private String passwordHash;

    @Column(nullable = false)
    private String nickname;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
