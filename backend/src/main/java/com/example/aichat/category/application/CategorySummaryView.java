package com.example.aichat.category.application;

import com.example.aichat.category.domain.Category;
import com.example.aichat.category.domain.CategoryScope;

public record CategorySummaryView(
        Long id,
        CategoryScope scope,
        String slug,
        String name
) {

    public static CategorySummaryView from(Category category) {
        return new CategorySummaryView(
                category.getId(),
                category.getScope(),
                category.getSlug(),
                category.getName()
        );
    }
}
