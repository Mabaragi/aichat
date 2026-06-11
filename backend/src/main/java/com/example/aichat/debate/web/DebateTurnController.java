package com.example.aichat.debate.web;

import com.example.aichat.common.exception.ErrorResponse;
import com.example.aichat.common.security.WebActor;
import com.example.aichat.debate.application.GenerateDebateTurnCommand;
import com.example.aichat.debate.application.GenerateDebateTurnUseCase;
import com.example.aichat.debate.application.ListDebateTurnsCommand;
import com.example.aichat.debate.application.ListDebateTurnsUseCase;
import com.example.aichat.debate.application.DebateTurnView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/debate-sessions/{sessionId}/turns")
@Tag(name = "Debate Turns", description = "Generate and inspect debate turns.")
public class DebateTurnController {

    private final GenerateDebateTurnUseCase generateDebateTurnUseCase;
    private final ListDebateTurnsUseCase listDebateTurnsUseCase;

    public DebateTurnController(GenerateDebateTurnUseCase generateDebateTurnUseCase,
                                ListDebateTurnsUseCase listDebateTurnsUseCase) {
        this.generateDebateTurnUseCase = generateDebateTurnUseCase;
        this.listDebateTurnsUseCase = listDebateTurnsUseCase;
    }

    @Operation(
            summary = "Generate the next debate turn",
            description = "Generates the next debate turn for the session and stores it.",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = false)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Debate turn generated.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GenerateTurnResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "sessionId": 1,
                                      "participantId": 1,
                                      "round": 1,
                                      "turnIndex": 1,
                                      "type": "ARGUMENT",
                                      "status": "COMPLETED",
                                      "content": "부먹은 소스와 튀김의 조화를 극대화한다는 점에서 더 완성도 높은 방식입니다.",
                                      "modelName": "mock-model",
                                      "inputTokens": 0,
                                      "outputTokens": 0,
                                      "createdAt": "2026-06-05T12:02:00"
                                    }
                                    """))),
            @ApiResponse(responseCode = "409", description = "Invalid session state or debate rule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication is required.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Session or participant not found, or inaccessible.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Generation provider failed.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/generate")
    public ResponseEntity<GenerateTurnResponse> generate(
            @Parameter(hidden = true)
            Authentication authentication,
            @PathVariable Long sessionId) {
        DebateTurnView generated = generateDebateTurnUseCase.execute(
                new GenerateDebateTurnCommand(WebActor.from(authentication), sessionId)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GenerateTurnResponse.from(generated));
    }

    @Operation(
            summary = "List debate turns",
            description = "Returns generated turns in turnIndex order.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Debate turn list returned.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(
                                    implementation = DebateTurnResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Authentication is required.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Session or participant not found, or inaccessible.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<DebateTurnResponse>> list(
            @Parameter(hidden = true)
            Authentication authentication,
            @PathVariable Long sessionId) {
        List<DebateTurnResponse> turns = listDebateTurnsUseCase.execute(
                new ListDebateTurnsCommand(WebActor.from(authentication), sessionId)
        ).stream()
                .map(DebateTurnResponse::from)
                .toList();
        return ResponseEntity.ok(turns);
    }
}
