package com.example.aichat.auth.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for user login.")
public record LoginRequest(
        @Schema(description = "User email address.", example = "owner@example.com")
        @NotBlank @Email String email,
        @Schema(description = "Raw password for the account.", example = "password123")
        @NotBlank String password
) {
}
