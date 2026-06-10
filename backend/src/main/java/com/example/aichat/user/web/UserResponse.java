package com.example.aichat.user.web;

import com.example.aichat.user.application.UserView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "User profile response.")
public record UserResponse(
        @Schema(description = "User identifier.", example = "1")
        Long id,
        @Schema(description = "User email address.", example = "owner@example.com")
        String email,
        @Schema(description = "User nickname.", example = "owner")
        String nickname,
        @Schema(description = "Timestamp when the user was created.",
                example = "2026-06-10T12:00:00")
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
