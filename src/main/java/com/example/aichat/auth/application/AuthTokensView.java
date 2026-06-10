package com.example.aichat.auth.application;

import com.example.aichat.user.application.UserView;

public record AuthTokensView(
        UserView user,
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {
}
