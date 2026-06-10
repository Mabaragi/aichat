package com.example.aichat.auth.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for user sign-up.")
public record SignupRequest(
        @Schema(description = "User email address.", example = "owner@example.com")
        @NotBlank @Email String email,
        @Schema(description = "Raw password for the new account.", example = "password123")
        @NotBlank @Size(min = 8, max = 72) String password,
        @Schema(description = "Display nickname for the user.", example = "owner")
        @NotBlank String nickname
) {
}
