package com.example.aichat.user.application;

public record CreateUserCommand(
        String email,
        String nickname
) {
}
