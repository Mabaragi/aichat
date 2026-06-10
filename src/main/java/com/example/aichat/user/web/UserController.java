package com.example.aichat.user.web;

import com.example.aichat.common.security.WebActor;
import com.example.aichat.user.application.GetUserUseCase;
import com.example.aichat.user.application.UserView;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final GetUserUseCase getUserUseCase;

    public UserController(GetUserUseCase getUserUseCase) {
        this.getUserUseCase = getUserUseCase;
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        UserView user = getUserUseCase.execute(WebActor.from(authentication).userId());
        return UserResponse.from(user);
    }
}
