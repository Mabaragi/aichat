package com.example.aichat.auth.web;

import com.example.aichat.auth.application.AuthTokensView;
import com.example.aichat.user.web.UserResponse;

public record AuthTokensResponse(
        UserResponse user,
        String accessToken,
        String refreshToken,
        String tokenType,
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
