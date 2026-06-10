package com.example.aichat.user.application;

import com.example.aichat.common.exception.BusinessException;
import com.example.aichat.common.exception.ErrorCode;
import com.example.aichat.user.domain.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class GetUserUseCase {

    private final UserRepository userRepository;

    public GetUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserView execute(Long userId) {
        return UserView.from(userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.USER_NOT_FOUND,
                        "User not found: " + userId
                )));
    }
}
