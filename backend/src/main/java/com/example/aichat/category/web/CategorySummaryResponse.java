package com.example.aichat.category.web;

import com.example.aichat.category.application.CategorySummaryView;
import com.example.aichat.category.domain.CategoryScope;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Category summary response.")
public record CategorySummaryResponse(
        @Schema(description = "Category identifier.", example = "1")
        Long id,
        @Schema(description = "Category scope.", example = "DEBATE")
        CategoryScope scope,
        @Schema(description = "Stable category slug.", example = "food")
        String slug,
        @Schema(description = "Display category name.", example = "음식")
        String name
) {

    public static CategorySummaryResponse from(CategorySummaryView view) {
        if (view == null) {
            return null;
        }
        return new CategorySummaryResponse(
                view.id(),
                view.scope(),
                view.slug(),
                view.name()
        );
    }
}
