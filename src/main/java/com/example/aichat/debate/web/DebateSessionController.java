package com.example.aichat.debate.web;

import com.example.aichat.common.exception.ErrorResponse;
import com.example.aichat.debate.application.CreateDebateParticipantCommand;
import com.example.aichat.debate.application.CreateDebateSessionCommand;
import com.example.aichat.debate.application.CreateDebateSessionUseCase;
import com.example.aichat.debate.application.DebateSessionView;
import com.example.aichat.common.security.RequestActor;
import com.example.aichat.common.security.WebActor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/debate-sessions")
@Tag(name = "Debate Sessions", description = "Create and manage debate sessions.")
public class DebateSessionController {

    private final CreateDebateSessionUseCase createDebateSessionUseCase;
    private final ObjectMapper objectMapper;

    public DebateSessionController(CreateDebateSessionUseCase createDebateSessionUseCase,
                                   ObjectMapper objectMapper) {
        this.createDebateSessionUseCase = createDebateSessionUseCase;
        this.objectMapper = objectMapper;
    }

    @Operation(
            summary = "Create a debate session",
            description = "Creates a debate session with exactly two participant snapshots.",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Debate session definition.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreateDebateSessionRequest.class),
                            examples = @ExampleObject(
                                    name = "ProsAndConsDebate",
                                    value = """
                                            {
                                              "topic": {
                                                "title": "Sauce-first vs dip-first",
                                                "description": "Which serving style creates the better eating experience?",
                                                "category": "FOOD"
                                              },
                                              "format": "PROS_AND_CONS",
                                              "maxRounds": 5,
                                              "maxTurnLength": 600,
                                              "participants": [
                                                {"characterId": 10, "model": "FAST"},
                                                {"characterId": 20, "model": "QUALITY"}
                                              ]
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Debate session created.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DebateSessionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid debate session request.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication is required.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User or character not found, or inaccessible character selected.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<DebateSessionResponse> create(
            @Parameter(hidden = true)
            Authentication authentication,
            @Valid @RequestBody CreateDebateSessionRequest request) {
        DebateSessionView created = createDebateSessionUseCase.execute(
                toCommand(WebActor.from(authentication), request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DebateSessionResponse.from(created, objectMapper));
    }

    private static CreateDebateSessionCommand toCommand(
            RequestActor actor, CreateDebateSessionRequest request) {
        return new CreateDebateSessionCommand(
                actor,
                actor.userId(),
                request.topic().title(),
                request.topic().description(),
                request.topic().category(),
                request.format(),
                request.maxRounds(),
                request.maxTurnLength(),
                request.participants().stream()
                        .map(participant -> new CreateDebateParticipantCommand(
                                participant.characterId(),
                                participant.model()
                        ))
                        .toList()
        );
    }
}
