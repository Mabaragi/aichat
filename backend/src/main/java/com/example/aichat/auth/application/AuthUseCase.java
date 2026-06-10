package com.example.aichat.auth.application;

import com.example.aichat.auth.domain.RefreshToken;
import com.example.aichat.auth.domain.RefreshTokenRepository;
import com.example.aichat.common.exception.BusinessException;
import com.example.aichat.common.exception.ErrorCode;
import com.example.aichat.common.time.TimeProvider;
import com.example.aichat.user.application.CreateUserCommand;
import com.example.aichat.user.application.CreateUserUseCase;
import com.example.aichat.user.application.UserView;
import com.example.aichat.user.domain.User;
import com.example.aichat.user.domain.UserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class AuthUseCase {

    private final CreateUserUseCase createUserUseCase;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;
    private final TimeProvider timeProvider;
    private final JwtProperties properties;
    private final RefreshTokenRotationService rotationService;

    public AuthUseCase(CreateUserUseCase createUserUseCase, UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository, JwtTokenService jwtTokenService,
                       PasswordEncoder passwordEncoder, TimeProvider timeProvider,
                       JwtProperties properties,
                       RefreshTokenRotationService rotationService) {
        this.createUserUseCase = createUserUseCase;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenService = jwtTokenService;
        this.passwordEncoder = passwordEncoder;
        this.timeProvider = timeProvider;
        this.properties = properties;
        this.rotationService = rotationService;
    }

    @Transactional
    public AuthTokensView signup(String email, String password, String nickname) {
        UserView created = createUserUseCase.execute(new CreateUserCommand(email, password, nickname));
        return issuePair(created, UUID.randomUUID(), UUID.randomUUID().toString());
    }

    @Transactional
    public AuthTokensView login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .filter(found -> passwordEncoder.matches(password, found.getPasswordHash()))
                .orElseThrow(AuthUseCase::invalidCredentials);
        return issuePair(UserView.from(user), UUID.randomUUID(), UUID.randomUUID().toString());
    }

    public synchronized AuthTokensView refresh(String rawRefreshToken) {
        Jwt jwt = jwtTokenService.decodeRefreshToken(rawRefreshToken);
        String tokenHash = jwtTokenService.hash(rawRefreshToken);
        LocalDateTime now = timeProvider.now();
        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(AuthUseCase::invalidToken);

        Long userId = parseUserId(jwt);
        if (!stored.getUserId().equals(userId)
                || !stored.getFamilyId().toString().equals(jwt.getClaimAsString("family"))
                || !stored.getSessionId().equals(jwt.getClaimAsString("sid"))) {
            throw invalidToken();
        }

        UserView user = UserView.from(userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.USER_NOT_FOUND,
                        "User not found: " + userId
                )));
        Instant instantNow = toInstant(now);
        IssuedToken access = jwtTokenService.issueAccessToken(userId, instantNow);
        IssuedToken refresh = jwtTokenService.issueRefreshToken(
                userId, stored.getFamilyId(), stored.getSessionId(), instantNow
        );
        RefreshToken replacement = toRefreshToken(userId, refresh);
        RefreshTokenRotationService.RotationResult rotation =
                rotationService.rotate(tokenHash, replacement, now);
        if (rotation == RefreshTokenRotationService.RotationResult.REUSED) {
            throw new BusinessException(
                    ErrorCode.REFRESH_TOKEN_REUSED,
                    "Refresh token has already been used or revoked"
            );
        }
        return response(user, access, refresh);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        String hash = jwtTokenService.hash(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(token ->
                refreshTokenRepository.revokeFamily(token.getFamilyId(), timeProvider.now()));
    }

    private AuthTokensView issuePair(UserView user, UUID familyId, String sessionId) {
        Instant now = toInstant(timeProvider.now());
        IssuedToken access = jwtTokenService.issueAccessToken(user.id(), now);
        IssuedToken refresh = jwtTokenService.issueRefreshToken(user.id(), familyId, sessionId, now);
        saveRefresh(user.id(), refresh);
        return response(user, access, refresh);
    }

    private void saveRefresh(Long userId, IssuedToken refresh) {
        refreshTokenRepository.save(toRefreshToken(userId, refresh));
    }

    private static RefreshToken toRefreshToken(Long userId, IssuedToken refresh) {
        return RefreshToken.issue(
                userId,
                refresh.familyId(),
                refresh.sessionId(),
                refresh.tokenHash(),
                LocalDateTime.ofInstant(refresh.expiresAt(), ZoneId.systemDefault())
        );
    }

    private AuthTokensView response(UserView user, IssuedToken access, IssuedToken refresh) {
        return new AuthTokensView(
                user,
                access.value(),
                refresh.value(),
                "Bearer",
                properties.accessTokenSeconds()
        );
    }

    private static Long parseUserId(Jwt jwt) {
        try {
            return Long.valueOf(jwt.getSubject());
        } catch (RuntimeException exception) {
            throw invalidToken();
        }
    }

    private static Instant toInstant(LocalDateTime time) {
        return time.atZone(ZoneId.systemDefault()).toInstant();
    }

    private static BusinessException invalidCredentials() {
        return new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Email or password is invalid");
    }

    private static BusinessException invalidToken() {
        return new BusinessException(ErrorCode.INVALID_TOKEN, "Token is invalid or expired");
    }
}
