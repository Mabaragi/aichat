package com.example.aichat.user.domain;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Locale;

@Getter
public class User {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final String nickname;
    private final LocalDateTime createdAt;

    public User(Long id, String email, String passwordHash, String nickname, LocalDateTime createdAt) {
        validateEmail(email);
        validatePasswordHash(passwordHash);
        validateNickname(nickname);
        validateCreatedAt(createdAt);

        this.id = id;
        this.email = normalizeEmail(email);
        this.passwordHash = passwordHash;
        this.nickname = nickname.trim();
        this.createdAt = createdAt;
    }

    public static User create(String email, String passwordHash, String nickname, LocalDateTime createdAt) {
        return new User(null, email, passwordHash, nickname, createdAt);
    }

    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private static void validatePasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("passwordHash is required");
        }
    }

    private static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email is required");
        }
    }

    private static void validateNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("nickname is required");
        }
    }

    private static void validateCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt is required");
        }
    }
}
