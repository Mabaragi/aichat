package com.example.aichat.debate.web;

import com.example.aichat.debate.application.CreateDebateParticipantCommand;
import com.example.aichat.debate.application.CreateDebateSessionCommand;
import com.example.aichat.debate.application.CreateDebateSessionUseCase;
import com.example.aichat.debate.application.DebateSessionView;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/debate-sessions")
public class DebateSessionController {

    private final CreateDebateSessionUseCase createDebateSessionUseCase;
    private final ObjectMapper objectMapper;

    public DebateSessionController(CreateDebateSessionUseCase createDebateSessionUseCase,
                                   ObjectMapper objectMapper) {
        this.createDebateSessionUseCase = createDebateSessionUseCase;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public ResponseEntity<DebateSessionResponse> create(
            @Valid @RequestBody CreateDebateSessionRequest request) {
        DebateSessionView created = createDebateSessionUseCase.execute(toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DebateSessionResponse.from(created, objectMapper));
    }

    private static CreateDebateSessionCommand toCommand(CreateDebateSessionRequest request) {
        return new CreateDebateSessionCommand(
                request.ownerId(),
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
