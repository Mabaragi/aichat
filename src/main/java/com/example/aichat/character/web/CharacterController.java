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
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@RestController
@RequestMapping("/api/characters")
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

    @PostMapping
    public ResponseEntity<CharacterResponse> create(@Valid @RequestBody CreateCharacterRequest request) {
        CharacterView created = createCharacterUseCase.execute(toCreateCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @GetMapping("/{characterId}")
    public CharacterResponse get(@PathVariable Long characterId) {
        return toResponse(getCharacterUseCase.execute(characterId));
    }

    @GetMapping
    public List<CharacterResponse> list(@RequestParam Long ownerId) {
        return listCharactersUseCase.execute(ownerId)
                .items()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PatchMapping("/{characterId}")
    public CharacterResponse update(@PathVariable Long characterId,
                                    @Valid @RequestBody UpdateCharacterRequest request) {
        CharacterView updated = updateCharacterUseCase.execute(toUpdateCommand(characterId, request));
        return toResponse(updated);
    }

    @DeleteMapping("/{characterId}")
    public ResponseEntity<Void> delete(@PathVariable Long characterId) {
        deleteCharacterUseCase.execute(characterId);
        return ResponseEntity.noContent().build();
    }

    private CreateCharacterCommand toCreateCommand(CreateCharacterRequest request) {
        return new CreateCharacterCommand(
                request.ownerId(),
                request.name(),
                request.description(),
                toPersonality(request.personality()),
                toSpeechStyle(request.speechStyle()),
                request.visibility()
        );
    }

    private UpdateCharacterCommand toUpdateCommand(Long characterId, UpdateCharacterRequest request) {
        return new UpdateCharacterCommand(
                characterId,
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

    private static Personality toPersonality(JsonNode node) {
        return node == null ? null : Personality.of(node.toString());
    }

    private static SpeechStyle toSpeechStyle(JsonNode node) {
        return node == null ? null : SpeechStyle.of(node.toString());
    }
}
