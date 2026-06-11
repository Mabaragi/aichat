package com.example.aichat.character.web;

import com.example.aichat.character.application.CharacterView;
import com.example.aichat.character.application.CreateCharacterCommand;
import com.example.aichat.character.application.CreateCharacterUseCase;
import com.example.aichat.character.application.DeleteCharacterUseCase;
import com.example.aichat.character.application.GetCharacterUseCase;
import com.example.aichat.character.application.ListCharactersUseCase;
import com.example.aichat.character.application.UpdateCharacterCommand;
import com.example.aichat.character.application.UpdateCharacterUseCase;
import com.example.aichat.character.domain.Personality;
import com.example.aichat.character.domain.SpeechStyle;
import com.example.aichat.common.exception.ErrorResponse;
import com.example.aichat.common.security.RequestActor;
import com.example.aichat.common.security.WebActor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/characters")
@Tag(name = "Characters", description = "Create, read, update, and delete debate characters.")
public class CharacterController {

    private final CreateCharacterUseCase createCharacterUseCase;
    private final GetCharacterUseCase getCharacterUseCase;
    private final ListCharactersUseCase listCharactersUseCase;
    private final UpdateCharacterUseCase updateCharacterUseCase;
    private final DeleteCharacterUseCase deleteCharacterUseCase;
    private final ObjectMapper objectMapper;

    public CharacterController(CreateCharacterUseCase createCharacterUseCase,
                               GetCharacterUseCase getCharacterUseCase,
                               ListCharactersUseCase listCharactersUseCase,
                               UpdateCharacterUseCase updateCharacterUseCase,
                               DeleteCharacterUseCase deleteCharacterUseCase,
                               ObjectMapper objectMapper) {
        this.createCharacterUseCase = createCharacterUseCase;
        this.getCharacterUseCase = getCharacterUseCase;
        this.listCharactersUseCase = listCharactersUseCase;
        this.updateCharacterUseCase = updateCharacterUseCase;
        this.deleteCharacterUseCase = deleteCharacterUseCase;
        this.objectMapper = objectMapper;
    }

    @Operation(
            summary = "Create a character",
            description = "Creates a character owned by the authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Character created.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CharacterResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid character request.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication is required.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CharacterResponse> create(
            @Parameter(hidden = true)
            Authentication authentication,
            @Valid @RequestBody CreateCharacterRequest request) {
        RequestActor actor = WebActor.from(authentication);
        CharacterView created = createCharacterUseCase.execute(toCreateCommand(actor, request));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @Operation(
            summary = "Get a character",
            description = "Returns a character by id. Anonymous callers can read only PUBLIC characters."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Character found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CharacterResponse.class))),
            @ApiResponse(responseCode = "404", description = "Character not found or hidden from the caller.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{characterId}")
    public CharacterResponse get(@Parameter(hidden = true) Authentication authentication,
                                 @Parameter(description = "Character identifier.", example = "1")
                                 @PathVariable Long characterId) {
        return toResponse(getCharacterUseCase.execute(
                WebActor.from(authentication), characterId));
    }

    @Operation(
            summary = "List characters by owner",
            description = "Returns visible characters for the requested owner. Anonymous callers receive only PUBLIC characters."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Characters retrieved.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(
                                    implementation = CharacterResponse.class))))
    })
    @GetMapping
    public List<CharacterResponse> list(@Parameter(hidden = true) Authentication authentication,
                                        @Parameter(description = "Owner user identifier.", example = "1")
                                        @RequestParam Long ownerId) {
        return listCharactersUseCase.execute(WebActor.from(authentication), ownerId)
                .items()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Operation(
            summary = "Update a character",
            description = "Partially updates a character owned by the authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Character updated.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CharacterResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid update request.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication is required.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Character not found or hidden from the caller.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{characterId}")
    public CharacterResponse update(@Parameter(hidden = true) Authentication authentication,
                                    @Parameter(description = "Character identifier.", example = "1")
                                    @PathVariable Long characterId,
                                    @Valid @RequestBody UpdateCharacterRequest request) {
        CharacterView updated = updateCharacterUseCase.execute(
                toUpdateCommand(WebActor.from(authentication), characterId, request));
        return toResponse(updated);
    }

    @Operation(
            summary = "Delete a character",
            description = "Deletes a character owned by the authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Character deleted."),
            @ApiResponse(responseCode = "401", description = "Authentication is required.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Character not found or hidden from the caller.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{characterId}")
    public ResponseEntity<Void> delete(@Parameter(hidden = true) Authentication authentication,
                                       @Parameter(description = "Character identifier.", example = "1")
                                       @PathVariable Long characterId) {
        deleteCharacterUseCase.execute(WebActor.from(authentication), characterId);
        return ResponseEntity.noContent().build();
    }

    private CreateCharacterCommand toCreateCommand(RequestActor actor,
                                                   CreateCharacterRequest request) {
        return new CreateCharacterCommand(
                actor,
                actor.userId(),
                request.category(),
                request.name(),
                request.description(),
                toPersonality(request.personality()),
                toSpeechStyle(request.speechStyle()),
                request.visibility()
        );
    }

    private UpdateCharacterCommand toUpdateCommand(RequestActor actor, Long characterId,
                                                   UpdateCharacterRequest request) {
        return new UpdateCharacterCommand(
                actor,
                characterId,
                request.category(),
                request.name(),
                request.description(),
                toPersonality(request.personality()),
                toSpeechStyle(request.speechStyle()),
                request.visibility()
        );
    }

    private CharacterResponse toResponse(CharacterView view) {
        return CharacterResponse.from(view, objectMapper);
    }

    private Personality toPersonality(Map<String, Object> value) {
        return value == null ? null : Personality.of(toJson(value));
    }

    private SpeechStyle toSpeechStyle(Map<String, Object> value) {
        return value == null ? null : SpeechStyle.of(toJson(value));
    }

    private String toJson(Map<String, Object> value) {
        return objectMapper.writeValueAsString(value);
    }
}
