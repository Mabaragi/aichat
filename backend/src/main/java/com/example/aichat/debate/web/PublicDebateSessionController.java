package com.example.aichat.debate.web;

import com.example.aichat.common.exception.ErrorResponse;
import com.example.aichat.common.web.PagedResponse;
import com.example.aichat.debate.application.GetPublicDebateSessionUseCase;
import com.example.aichat.debate.application.ListPublicDebateSessionsUseCase;
import com.example.aichat.debate.application.ListPublicDebateTurnsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

import java.util.List;

@RestController
@RequestMapping("/api/public/debate-sessions")
@Tag(name = "Public Debate Sessions", description = "Browse completed public debates.")
public class PublicDebateSessionController {

    private final ListPublicDebateSessionsUseCase listPublicDebateSessionsUseCase;
    private final GetPublicDebateSessionUseCase getPublicDebateSessionUseCase;
    private final ListPublicDebateTurnsUseCase listPublicDebateTurnsUseCase;
    private final ObjectMapper objectMapper;

    public PublicDebateSessionController(
            ListPublicDebateSessionsUseCase listPublicDebateSessionsUseCase,
            GetPublicDebateSessionUseCase getPublicDebateSessionUseCase,
            ListPublicDebateTurnsUseCase listPublicDebateTurnsUseCase,
            ObjectMapper objectMapper) {
        this.listPublicDebateSessionsUseCase = listPublicDebateSessionsUseCase;
        this.getPublicDebateSessionUseCase = getPublicDebateSessionUseCase;
        this.listPublicDebateTurnsUseCase = listPublicDebateTurnsUseCase;
        this.objectMapper = objectMapper;
    }

    @Operation(summary = "List public completed debates",
            description = "Returns PUBLIC and COMPLETED debate sessions filtered by query and category.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Public debate sessions returned.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PublicDebateSessionPageResponse.class))),
            @ApiResponse(responseCode = "404", description = "Category not found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public PublicDebateSessionPageResponse list(
            @RequestParam(required = false) String query,
            @Parameter(description = "Debate category slug.", example = "food")
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        return PublicDebateSessionPageResponse.from(PagedResponse.from(
                listPublicDebateSessionsUseCase.execute(query, category, page, size),
                view -> DebateSessionResponse.from(view, objectMapper)
        ));
    }

    @Operation(summary = "Get a public completed debate",
            description = "Returns a PUBLIC and COMPLETED debate session by id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Public debate session returned.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DebateSessionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Debate session not found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{sessionId}")
    public DebateSessionResponse get(@PathVariable Long sessionId) {
        return DebateSessionResponse.from(
                getPublicDebateSessionUseCase.execute(sessionId),
                objectMapper
        );
    }

    @Operation(summary = "List turns for a public completed debate",
            description = "Returns generated turns for a PUBLIC and COMPLETED debate in turnIndex order.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Public debate turns returned.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = DebateTurnResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Debate session not found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{sessionId}/turns")
    public List<DebateTurnResponse> turns(@PathVariable Long sessionId) {
        return listPublicDebateTurnsUseCase.execute(sessionId)
                .stream()
                .map(DebateTurnResponse::from)
                .toList();
    }

    @Schema(name = "PublicDebateSessionPageResponse")
    public record PublicDebateSessionPageResponse(
            List<DebateSessionResponse> items,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext
    ) {
        static PublicDebateSessionPageResponse from(PagedResponse<DebateSessionResponse> response) {
            return new PublicDebateSessionPageResponse(
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
