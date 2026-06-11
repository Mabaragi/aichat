package com.example.aichat.debate.web;

import com.example.aichat.common.exception.ErrorResponse;
import com.example.aichat.debate.application.CreateDebateParticipantCommand;
import com.example.aichat.debate.application.CreateDebateSessionCommand;
import com.example.aichat.debate.application.CreateDebateSessionUseCase;
import com.example.aichat.debate.application.DebateSessionView;
import com.example.aichat.debate.application.StartDebateSessionCommand;
import com.example.aichat.debate.application.StartDebateSessionUseCase;
import com.example.aichat.debate.application.CompleteDebateSessionCommand;
import com.example.aichat.debate.application.CompleteDebateSessionUseCase;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/debate-sessions")
@Tag(name = "Debate Sessions", description = "Create and manage debate sessions.")
public class DebateSessionController {

    private final CreateDebateSessionUseCase createDebateSessionUseCase;
    private final StartDebateSessionUseCase startDebateSessionUseCase;
    private final CompleteDebateSessionUseCase completeDebateSessionUseCase;
    private final ObjectMapper objectMapper;

    public DebateSessionController(CreateDebateSessionUseCase createDebateSessionUseCase,
                                   StartDebateSessionUseCase startDebateSessionUseCase,
                                   CompleteDebateSessionUseCase completeDebateSessionUseCase,
                                   ObjectMapper objectMapper) {
        this.createDebateSessionUseCase = createDebateSessionUseCase;
        this.startDebateSessionUseCase = startDebateSessionUseCase;
        this.completeDebateSessionUseCase = completeDebateSessionUseCase;
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
                                              "category": "food"
                                            },
                                            "format": "PROS_AND_CONS",
                                            "visibility": "PUBLIC",
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

    @Operation(
            summary = "Start a debate session",
            description = "Transitions a created debate session to RUNNING.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Debate session started.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = StartDebateSessionResponse.class))),
            @ApiResponse(responseCode = "409", description = "Invalid session state.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication is required.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Session not found or inaccessible.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{sessionId}/start")
    public ResponseEntity<StartDebateSessionResponse> start(
            @Parameter(hidden = true)
            Authentication authentication,
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(StartDebateSessionResponse.from(
                startDebateSessionUseCase.execute(new StartDebateSessionCommand(
                        WebActor.from(authentication),
                        sessionId
                ))
        ));
    }

    @Operation(
            summary = "Complete a debate session",
            description = "Transitions a running debate session to COMPLETED.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Debate session completed.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CompleteDebateSessionResponse.class))),
            @ApiResponse(responseCode = "409", description = "Invalid session state.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication is required.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Session not found or inaccessible.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{sessionId}/complete")
    public ResponseEntity<CompleteDebateSessionResponse> complete(
            @Parameter(hidden = true)
            Authentication authentication,
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(CompleteDebateSessionResponse.from(
                completeDebateSessionUseCase.execute(new CompleteDebateSessionCommand(
                        WebActor.from(authentication),
                        sessionId
                ))
        ));
    }

    private static CreateDebateSessionCommand toCommand(
            RequestActor actor, CreateDebateSessionRequest request) {
        return new CreateDebateSessionCommand(
                actor,
                actor.userId(),
                request.topic().title(),
                request.topic().description(),
                request.topic().category(),
                request.visibility(),
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
