package com.example.aichat.user.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Test
    void createUserWithRequiredFields() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 6, 5, 12, 0);

        User user = User.create(" USER@example.com ", "{bcrypt}hash", "마바라기", createdAt);

        assertThat(user.getId()).isNull();
        assertThat(user.getEmail()).isEqualTo("user@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("{bcrypt}hash");
        assertThat(user.getNickname()).isEqualTo("마바라기");
        assertThat(user.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void rejectUserWithBlankEmail() {
        assertThatThrownBy(() -> User.create("   ", "{bcrypt}hash", "마바라기", defaultNow()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("email is required");
    }

    @Test
    void rejectUserWithBlankNickname() {
        assertThatThrownBy(() -> User.create(
                "user@example.com", "{bcrypt}hash", "   ", defaultNow()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("nickname is required");
    }

    @Test
    void rejectUserWithNullCreatedAt() {
        assertThatThrownBy(() -> User.create(
                "user@example.com", "{bcrypt}hash", "마바라기", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("createdAt is required");
    }

    private static LocalDateTime defaultNow() {
        return LocalDateTime.of(2026, 6, 5, 12, 0);
    }
}
