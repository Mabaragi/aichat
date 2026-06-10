package com.example.aichat.common.security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.Authentication;

public final class WebActor {

    private WebActor() {
    }

    public static RequestActor from(Jwt jwt) {
        return jwt == null
                ? RequestActor.anonymous()
                : RequestActor.authenticated(Long.valueOf(jwt.getSubject()));
    }

    public static RequestActor from(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return RequestActor.anonymous();
        }
        return from(jwt);
    }
}
