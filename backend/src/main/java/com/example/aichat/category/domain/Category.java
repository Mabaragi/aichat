package com.example.aichat.category.domain;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Locale;

@Getter
public class Category {

    public static final String DEFAULT_SLUG = "other";

    private final Long id;
    private final CategoryScope scope;
    private final String slug;
    private final String name;
    private final String description;
    private final int displayOrder;
    private final boolean active;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public Category(Long id, CategoryScope scope, String slug, String name,
                    String description, int displayOrder, boolean active,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        if (scope == null) {
            throw new IllegalArgumentException("scope is required");
        }
        if (slug == null || slug.isBlank()) {
            throw new IllegalArgumentException("slug is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt is required");
        }
        if (updatedAt == null) {
            throw new IllegalArgumentException("updatedAt is required");
        }

        this.id = id;
        this.scope = scope;
        this.slug = normalizeSlug(slug);
        this.name = name;
        this.description = description;
        this.displayOrder = displayOrder;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static String normalizeSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            return DEFAULT_SLUG;
        }
        return slug.trim().toLowerCase(Locale.ROOT);
    }
}
