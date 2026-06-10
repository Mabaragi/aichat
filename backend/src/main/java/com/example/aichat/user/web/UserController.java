package com.example.aichat.user.web;

import com.example.aichat.common.security.WebActor;
import com.example.aichat.user.application.GetUserUseCase;
import com.example.aichat.user.application.UserView;
import com.example.aichat.common.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Authenticated user account APIs.")
public class UserController {

    private final GetUserUseCase getUserUseCase;

    public UserController(GetUserUseCase getUserUseCase) {
        this.getUserUseCase = getUserUseCase;
    }

    @Operation(
            summary = "Get the current user",
            description = "Returns the user profile derived from the current JWT access token.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current user returned.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication is required.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Authenticated user not found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/me")
    public UserResponse me(@Parameter(hidden = true) Authentication authentication) {
        UserView user = getUserUseCase.execute(WebActor.from(authentication).userId());
        return UserResponse.from(user);
    }
}
