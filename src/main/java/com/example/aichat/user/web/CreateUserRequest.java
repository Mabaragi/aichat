package com.example.aichat.user.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank @Email String email,
        @NotBlank String nickname
) {
}
