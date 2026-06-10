package com.example.aichat.auth.application;

import com.example.aichat.common.exception.BusinessException;
import com.example.aichat.common.exception.ErrorCode;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Component
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class JwtTokenService {

    private static final String ACCESS = "access";
    private static final String REFRESH = "refresh";

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final JwtProperties properties;

    public JwtTokenService(JwtEncoder encoder,
                           @Qualifier("jwtTokenDecoder") JwtDecoder decoder,
                           JwtProperties properties) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.properties = properties;
    }

    public IssuedToken issueAccessToken(Long userId, Instant now) {
        String sessionId = UUID.randomUUID().toString();
        Instant expiresAt = now.plusSeconds(properties.accessTokenSeconds());
        String token = encode(userId, ACCESS, null, sessionId, now, expiresAt, List.of("USER"));
        return new IssuedToken(token, hash(token), null, sessionId, expiresAt);
    }

    public IssuedToken issueRefreshToken(Long userId, UUID familyId, String sessionId, Instant now) {
        Instant expiresAt = now.plusSeconds(properties.refreshTokenSeconds());
        String token = encode(userId, REFRESH, familyId, sessionId, now, expiresAt, List.of());
        return new IssuedToken(token, hash(token), familyId, sessionId, expiresAt);
    }

    public Jwt decodeRefreshToken(String token) {
        Jwt jwt = decode(token);
        if (!REFRESH.equals(jwt.getClaimAsString("type"))
                || jwt.getClaimAsString("family") == null
                || jwt.getClaimAsString("sid") == null) {
            throw invalidToken();
        }
        return jwt;
    }

    public Jwt decodeAccessToken(String token) {
        Jwt jwt = decode(token);
        if (!ACCESS.equals(jwt.getClaimAsString("type"))) {
            throw invalidToken();
        }
        return jwt;
    }

    public String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private String encode(Long userId, String type, UUID familyId, String sessionId,
                          Instant issuedAt, Instant expiresAt, List<String> roles) {
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .subject(userId.toString())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                .claim("type", type)
                .claim("sid", sessionId);
        if (familyId != null) {
            claims.claim("family", familyId.toString());
        }
        if (!roles.isEmpty()) {
            claims.claim("roles", roles);
        }

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
    }

    private Jwt decode(String token) {
        if (token == null || token.isBlank()) {
            throw invalidToken();
        }
        try {
            Jwt jwt = decoder.decode(token);
            if (!properties.issuer().equals(jwt.getClaimAsString("iss"))) {
                throw invalidToken();
            }
            return jwt;
        } catch (BusinessException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new BusinessException(
                    ErrorCode.INVALID_TOKEN,
                    "Token is invalid or expired",
                    exception
            );
        }
    }

    private static BusinessException invalidToken() {
        return new BusinessException(ErrorCode.INVALID_TOKEN, "Token is invalid or expired");
    }
}
