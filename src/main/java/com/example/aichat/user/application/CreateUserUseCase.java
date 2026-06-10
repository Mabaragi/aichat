package com.example.aichat.user.application;

import com.example.aichat.common.time.TimeProvider;
import com.example.aichat.common.exception.BusinessException;
import com.example.aichat.common.exception.ErrorCode;
import com.example.aichat.user.domain.User;
import com.example.aichat.user.domain.UserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class CreateUserUseCase {

    private final UserRepository userRepository;
    private final TimeProvider timeProvider;
    private final PasswordEncoder passwordEncoder;

    public CreateUserUseCase(UserRepository userRepository, TimeProvider timeProvider,
                             PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.timeProvider = timeProvider;
        this.passwordEncoder = passwordEncoder;
    }

    public UserView execute(CreateUserCommand command) {
        validatePassword(command.password());
        String normalizedEmail = User.normalizeEmail(command.email());
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessException(
                    ErrorCode.EMAIL_ALREADY_EXISTS,
                    "Email is already registered"
            );
        }

        User created = User.create(
                normalizedEmail,
                passwordEncoder.encode(command.password()),
                command.nickname(),
                timeProvider.now()
        );

        return UserView.from(userRepository.save(created));
    }

    private static void validatePassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 72) {
            throw new IllegalArgumentException("password must be between 8 and 72 characters");
        }
    }
}
