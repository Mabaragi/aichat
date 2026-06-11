package com.example.aichat.common.web;

import com.example.aichat.common.application.PagedResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Paged list response.")
public record PagedResponse<T>(
        @Schema(description = "Items in this page.")
        List<T> items,
        @Schema(description = "Zero-based page number.", example = "0")
        int page,
        @Schema(description = "Requested page size.", example = "20")
        int size,
        @Schema(description = "Total matching element count.", example = "42")
        long totalElements,
        @Schema(description = "Total page count.", example = "3")
        int totalPages,
        @Schema(description = "Whether another page is available.", example = "true")
        boolean hasNext
) {

    public static <T, R> PagedResponse<R> from(PagedResult<T> result,
                                               java.util.function.Function<T, R> mapper) {
        return new PagedResponse<>(
                result.items().stream().map(mapper).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext()
        );
    }
}
