package com.example.aichat.user.application;

import com.example.aichat.user.domain.User;

import java.time.LocalDateTime;

public record UserView(
        Long id,
        String email,
        String nickname,
        LocalDateTime createdAt
) {

    public static UserView from(User user) {
        return new UserView(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getCreatedAt()
        );
    }
}
