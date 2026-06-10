package com.example.aichat.auth.application;

import com.example.aichat.auth.domain.RefreshToken;
import com.example.aichat.auth.domain.RefreshTokenRepository;
import com.example.aichat.common.exception.BusinessException;
import com.example.aichat.common.exception.ErrorCode;
import com.example.aichat.common.time.TimeProvider;
import com.example.aichat.user.application.CreateUserUseCase;
import com.example.aichat.user.domain.User;
import com.example.aichat.user.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthUseCaseTest {

    private static final LocalDateTime NOW = LocalDateTime.now().withNano(0);

    private InMemoryUserRepository userRepository;
    private InMemoryRefreshTokenRepository refreshTokenRepository;
    private JwtTokenService tokenService;
    private AuthUseCase authUseCase;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        refreshTokenRepository = new InMemoryRefreshTokenRepository();
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        TimeProvider timeProvider = () -> NOW;
        JwtProperties properties = new JwtProperties("unused", "aichat", 900, 1_209_600);

        SecretKey key = new SecretKeySpec(
                "test-jwt-secret-key-that-is-at-least-32-bytes-long"
                        .getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        JwtEncoder encoder = NimbusJwtEncoder.withSecretKey(key)
                .algorithm(MacAlgorithm.HS256)
                .build();
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("aichat"));
        tokenService = new JwtTokenService(encoder, decoder, properties);

        CreateUserUseCase createUserUseCase =
                new CreateUserUseCase(userRepository, timeProvider, passwordEncoder);
        RefreshTokenRotationService rotationService =
                new RefreshTokenRotationService(refreshTokenRepository);
        authUseCase = new AuthUseCase(
                createUserUseCase,
                userRepository,
                refreshTokenRepository,
                tokenService,
                passwordEncoder,
                timeProvider,
                properties,
                rotationService
        );
    }

    @Test
    void signupNormalizesEmailHashesPasswordAndReturnsTokenPair() {
        AuthTokensView result = authUseCase.signup(
                " USER@example.com ",
                "password123",
                "nickname"
        );

        assertThat(result.user().email()).isEqualTo("user@example.com");
        assertThat(result.accessToken()).isNotBlank();
        assertThat(result.refreshToken()).isNotBlank();
        assertThat(result.expiresIn()).isEqualTo(900);
        assertThat(userRepository.findByEmail("user@example.com"))
                .hasValueSatisfying(user ->
                        assertThat(user.getPasswordHash()).isNotEqualTo("password123"));
        assertThat(tokenService.decodeAccessToken(result.accessToken()).getSubject())
                .isEqualTo(result.user().id().toString());
    }

    @Test
    void loginUsesSameFailureForUnknownEmailAndWrongPassword() {
        authUseCase.signup("user@example.com", "password123", "nickname");

        assertInvalidCredentials(() -> authUseCase.login("missing@example.com", "password123"));
        assertInvalidCredentials(() -> authUseCase.login("user@example.com", "wrong-password"));
    }

    @Test
    void refreshRotatesTokenAndReuseRevokesWholeFamily() {
        AuthTokensView signup = authUseCase.signup(
                "user@example.com",
                "password123",
                "nickname"
        );

        AuthTokensView rotated = authUseCase.refresh(signup.refreshToken());

        assertThat(rotated.refreshToken()).isNotEqualTo(signup.refreshToken());
        assertThatThrownBy(() -> authUseCase.refresh(signup.refreshToken()))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.REFRESH_TOKEN_REUSED));
        String rotatedHash = tokenService.hash(rotated.refreshToken());
        assertThat(refreshTokenRepository.findByTokenHash(rotatedHash))
                .hasValueSatisfying(token -> assertThat(token.getRevokedAt()).isEqualTo(NOW));
    }

    @Test
    void logoutIsIdempotentAndRevokesRefreshFamily() {
        AuthTokensView signup = authUseCase.signup(
                "user@example.com",
                "password123",
                "nickname"
        );

        authUseCase.logout(signup.refreshToken());
        authUseCase.logout(signup.refreshToken());
        authUseCase.logout(null);

        assertThatThrownBy(() -> authUseCase.refresh(signup.refreshToken()))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void concurrentRotationAllowsOnlyOneReplacement() throws Exception {
        UUID familyId = UUID.randomUUID();
        RefreshToken current = refreshTokenRepository.save(RefreshToken.issue(
                1L,
                familyId,
                "session",
                "current-hash",
                NOW.plusDays(1)
        ));
        RefreshTokenRotationService service =
                new RefreshTokenRotationService(refreshTokenRepository);

        Callable<RefreshTokenRotationService.RotationResult> first = () ->
                service.rotate(current.getTokenHash(), RefreshToken.issue(
                        1L, familyId, "session", "replacement-1", NOW.plusDays(1)), NOW);
        Callable<RefreshTokenRotationService.RotationResult> second = () ->
                service.rotate(current.getTokenHash(), RefreshToken.issue(
                        1L, familyId, "session", "replacement-2", NOW.plusDays(1)), NOW);

        try (var executor = Executors.newFixedThreadPool(2)) {
            var results = executor.invokeAll(java.util.List.of(first, second)).stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (Exception exception) {
                            throw new AssertionError(exception);
                        }
                    })
                    .toList();

            assertThat(results)
                    .containsExactlyInAnyOrder(
                            RefreshTokenRotationService.RotationResult.SUCCESS,
                            RefreshTokenRotationService.RotationResult.REUSED
                    );
        }
    }

    private static void assertInvalidCredentials(org.assertj.core.api.ThrowableAssert.ThrowingCallable call) {
        assertThatThrownBy(call)
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));
    }

    private static final class InMemoryUserRepository implements UserRepository {

        private final AtomicLong ids = new AtomicLong(1);
        private final Map<Long, User> users = new HashMap<>();

        @Override
        public User save(User user) {
            User saved = new User(
                    user.getId() == null ? ids.getAndIncrement() : user.getId(),
                    user.getEmail(),
                    user.getPasswordHash(),
                    user.getNickname(),
                    user.getCreatedAt()
            );
            users.put(saved.getId(), saved);
            return saved;
        }

        @Override
        public Optional<User> findById(Long userId) {
            return Optional.ofNullable(users.get(userId));
        }

        @Override
        public Optional<User> findByEmail(String email) {
            String normalized = User.normalizeEmail(email);
            return users.values().stream()
                    .filter(user -> user.getEmail().equals(normalized))
                    .findFirst();
        }
    }

    private static final class InMemoryRefreshTokenRepository
            implements RefreshTokenRepository {

        private final AtomicLong ids = new AtomicLong(1);
        private final Map<String, RefreshToken> tokens = new HashMap<>();

        @Override
        public synchronized RefreshToken save(RefreshToken token) {
            RefreshToken saved = token.getId() == null
                    ? new RefreshToken(
                            ids.getAndIncrement(),
                            token.getUserId(),
                            token.getFamilyId(),
                            token.getSessionId(),
                            token.getTokenHash(),
                            token.getExpiresAt(),
                            token.getRevokedAt(),
                            token.getReplacedByHash(),
                            token.getVersion()
                    )
                    : token;
            tokens.put(saved.getTokenHash(), saved);
            return saved;
        }

        @Override
        public synchronized Optional<RefreshToken> findByTokenHash(String tokenHash) {
            return Optional.ofNullable(tokens.get(tokenHash));
        }

        @Override
        public synchronized void revokeFamily(UUID familyId, LocalDateTime revokedAt) {
            tokens.values().stream()
                    .filter(token -> token.getFamilyId().equals(familyId))
                    .forEach(token -> token.revoke(revokedAt));
        }
    }
}
