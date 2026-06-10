package com.example.aichat.user.application;

import com.example.aichat.common.exception.BusinessException;
import com.example.aichat.common.exception.ErrorCode;
import com.example.aichat.common.time.TimeProvider;
import com.example.aichat.user.domain.User;
import com.example.aichat.user.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class UserUseCaseTest {

    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2026, 6, 5, 12, 0);

    private InMemoryUserRepository repository;
    private CreateUserUseCase createUserUseCase;
    private GetUserUseCase getUserUseCase;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
        TimeProvider timeProvider = () -> FIXED_TIME;
        passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        createUserUseCase = new CreateUserUseCase(repository, timeProvider, passwordEncoder);
        getUserUseCase = new GetUserUseCase(repository);
    }

    @Test
    void createAndGetUserFlowWorks() {
        UserView created = createUserUseCase.execute(new CreateUserCommand(
                " USER@example.com ",
                "password123",
                "마바라기"
        ));

        assertThat(created.id()).isEqualTo(1L);
        assertThat(created.email()).isEqualTo("user@example.com");
        assertThat(created.nickname()).isEqualTo("마바라기");
        assertThat(created.createdAt()).isEqualTo(FIXED_TIME);
        assertThat(repository.findById(created.id()))
                .hasValueSatisfying(user -> {
                    assertThat(user.getPasswordHash()).isNotEqualTo("password123");
                    assertThat(passwordEncoder.matches(
                            "password123", user.getPasswordHash())).isTrue();
                });

        UserView fetched = getUserUseCase.execute(created.id());

        assertThat(fetched).isEqualTo(created);
    }

    @Test
    void getUserThrowsWhenUserDoesNotExist() {
        BusinessException exception = catchThrowableOfType(
                () -> getUserUseCase.execute(99L),
                BusinessException.class
        );

        assertThat(exception).hasMessage("User not found: 99");
        assertThat(exception.getCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    private static final class InMemoryUserRepository implements UserRepository {

        private final Map<Long, User> users = new HashMap<>();
        private final AtomicLong nextId = new AtomicLong(1L);

        @Override
        public User save(User user) {
            User saved = new User(
                    user.getId() == null ? nextId.getAndIncrement() : user.getId(),
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
    }
}
