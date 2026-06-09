package com.example.aichat.user.domain;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class User {

    private final Long id;
    private final String email;
    private final String nickname;
    private final LocalDateTime createdAt;

    public User(Long id, String email, String nickname, LocalDateTime createdAt) {
        validateEmail(email);
        validateNickname(nickname);
        validateCreatedAt(createdAt);

        this.id = id;
        this.email = email.trim();
        this.nickname = nickname.trim();
        this.createdAt = createdAt;
    }

    public static User create(String email, String nickname, LocalDateTime createdAt) {
        return new User(null, email, nickname, createdAt);
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
