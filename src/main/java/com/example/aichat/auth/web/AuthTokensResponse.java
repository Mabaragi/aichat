package com.example.aichat.auth.web;

import com.example.aichat.auth.application.AuthTokensView;
import io.swagger.v3.oas.annotations.media.Schema;
import com.example.aichat.user.web.UserResponse;

@Schema(description = "Authentication response containing the current user and issued tokens.")
public record AuthTokensResponse(
        @Schema(description = "Current authenticated user.")
        UserResponse user,
        @Schema(description = "JWT access token for authenticated API calls.",
                example = "eyJhbGciOiJIUzI1NiJ9.access-token")
        String accessToken,
        @Schema(description = "Refresh token for access token renewal.",
                example = "eyJhbGciOiJIUzI1NiJ9.refresh-token")
        String refreshToken,
        @Schema(description = "HTTP authentication scheme.", example = "Bearer")
        String tokenType,
        @Schema(description = "Access token lifetime in seconds.", example = "900")
        long expiresIn
) {
    static AuthTokensResponse from(AuthTokensView view) {
        return new AuthTokensResponse(
                UserResponse.from(view.user()),
                view.accessToken(),
                view.refreshToken(),
                view.tokenType(),
                view.expiresIn()
        );
    }
}
