package com.example.aichat.category.infrastructure;

import com.example.aichat.category.domain.CategoryScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "categories",
        indexes = {
                @Index(name = "idx_categories_scope_order", columnList = "scope, active, display_order, id")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uk_categories_scope_slug",
                columnNames = {"scope", "slug"}
        )
)
public class CategoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryScope scope;

    @Column(nullable = false)
    private String slug;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected CategoryJpaEntity() {
    }

    CategoryJpaEntity(Long id, CategoryScope scope, String slug, String name,
                      String description, int displayOrder, boolean active,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.scope = scope;
        this.slug = slug;
        this.name = name;
        this.description = description;
        this.displayOrder = displayOrder;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    Long id() {
        return id;
    }

    CategoryScope scope() {
        return scope;
    }

    String slug() {
        return slug;
    }

    String name() {
        return name;
    }

    String description() {
        return description;
    }

    int displayOrder() {
        return displayOrder;
    }

    boolean active() {
        return active;
    }

    LocalDateTime createdAt() {
        return createdAt;
    }

    LocalDateTime updatedAt() {
        return updatedAt;
    }
}
