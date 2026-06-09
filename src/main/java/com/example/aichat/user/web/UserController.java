package com.example.aichat.user.web;

import com.example.aichat.user.application.CreateUserCommand;
import com.example.aichat.user.application.CreateUserUseCase;
import com.example.aichat.user.application.UserView;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;

    public UserController(CreateUserUseCase createUserUseCase) {
        this.createUserUseCase = createUserUseCase;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        UserView created = createUserUseCase.execute(new CreateUserCommand(
                request.email(),
                request.nickname()
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(created));
    }
}
