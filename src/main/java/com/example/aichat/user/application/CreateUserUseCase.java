package com.example.aichat.user.application;

import com.example.aichat.common.time.TimeProvider;
import com.example.aichat.user.domain.User;
import com.example.aichat.user.domain.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateUserUseCase {

    private final UserRepository userRepository;
    private final TimeProvider timeProvider;

    public CreateUserUseCase(UserRepository userRepository, TimeProvider timeProvider) {
        this.userRepository = userRepository;
        this.timeProvider = timeProvider;
    }

    public UserView execute(CreateUserCommand command) {
        User created = User.create(
                command.email(),
                command.nickname(),
                timeProvider.now()
        );

        return UserView.from(userRepository.save(created));
    }
}
