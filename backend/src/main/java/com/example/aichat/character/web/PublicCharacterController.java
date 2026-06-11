package com.example.aichat.character.web;

import com.example.aichat.character.application.GetPublicCharacterUseCase;
import com.example.aichat.character.application.ListPublicCharactersUseCase;
import com.example.aichat.common.exception.ErrorResponse;
import com.example.aichat.common.web.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/public/characters")
@Tag(name = "Public Characters", description = "Browse public character catalog.")
public class PublicCharacterController {

    private final ListPublicCharactersUseCase listPublicCharactersUseCase;
    private final GetPublicCharacterUseCase getPublicCharacterUseCase;
    private final ObjectMapper objectMapper;

    public PublicCharacterController(ListPublicCharactersUseCase listPublicCharactersUseCase,
                                     GetPublicCharacterUseCase getPublicCharacterUseCase,
                                     ObjectMapper objectMapper) {
        this.listPublicCharactersUseCase = listPublicCharactersUseCase;
        this.getPublicCharacterUseCase = getPublicCharacterUseCase;
        this.objectMapper = objectMapper;
    }

    @Operation(summary = "List public characters",
            description = "Returns PUBLIC characters filtered by query and category.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Public characters returned.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PublicCharacterPageResponse.class))),
            @ApiResponse(responseCode = "404", description = "Category not found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public PublicCharacterPageResponse list(
            @RequestParam(required = false) String query,
            @Parameter(description = "Character category slug.", example = "expert")
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        return PublicCharacterPageResponse.from(PagedResponse.from(
                listPublicCharactersUseCase.execute(query, category, page, size),
                view -> CharacterResponse.from(view, objectMapper)
        ));
    }

    @Operation(summary = "Get a public character",
            description = "Returns one PUBLIC character by id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Public character returned.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CharacterResponse.class))),
            @ApiResponse(responseCode = "404", description = "Character not found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{characterId}")
    public CharacterResponse get(@PathVariable Long characterId) {
        return CharacterResponse.from(getPublicCharacterUseCase.execute(characterId), objectMapper);
    }

    @Schema(name = "PublicCharacterPageResponse")
    public record PublicCharacterPageResponse(
            java.util.List<CharacterResponse> items,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext
    ) {
        static PublicCharacterPageResponse from(PagedResponse<CharacterResponse> response) {
            return new PublicCharacterPageResponse(
                    response.items(),
                    response.page(),
                    response.size(),
                    response.totalElements(),
                    response.totalPages(),
                    response.hasNext()
            );
        }
    }
}
