package com.example.aichat.user.web;

import com.example.aichat.user.application.UserView;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        String nickname,
        LocalDateTime createdAt
) {

    public static UserResponse from(UserView view) {
        return new UserResponse(
                view.id(),
                view.email(),
                view.nickname(),
                view.createdAt()
        );
    }
}
