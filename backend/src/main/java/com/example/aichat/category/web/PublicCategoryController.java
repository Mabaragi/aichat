package com.example.aichat.category.web;

import com.example.aichat.category.application.ListCategoriesUseCase;
import com.example.aichat.category.domain.CategoryScope;
import com.example.aichat.common.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/categories")
@Tag(name = "Public Categories", description = "Read public category metadata.")
public class PublicCategoryController {

    private final ListCategoriesUseCase listCategoriesUseCase;

    public PublicCategoryController(ListCategoriesUseCase listCategoriesUseCase) {
        this.listCategoriesUseCase = listCategoriesUseCase;
    }

    @Operation(summary = "List public categories",
            description = "Returns active categories for a resource scope.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categories returned.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(
                                    implementation = CategorySummaryResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid scope.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public List<CategorySummaryResponse> list(
            @Parameter(description = "Category scope.", example = "DEBATE")
            @RequestParam @NotNull CategoryScope scope) {
        return listCategoriesUseCase.execute(scope)
                .stream()
                .map(CategorySummaryResponse::from)
                .toList();
    }
}
