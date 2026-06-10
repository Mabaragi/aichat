package com.example.aichat.auth.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload carrying a refresh token.")
public record RefreshTokenRequest(
        @Schema(description = "Refresh token issued during sign-up or login.",
                example = "eyJhbGciOiJIUzI1NiJ9.refresh-token")
        @NotBlank String refreshToken
) {
}
