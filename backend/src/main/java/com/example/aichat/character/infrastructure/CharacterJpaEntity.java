package com.example.aichat.character.infrastructure;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "characters")
public class CharacterJpaEntity {
    protected CharacterJpaEntity() {
    }

    CharacterJpaEntity(Long id,
                       Long ownerId,
                       Long categoryId,
                       String name,
                       String description,
                       String persona,
                       String visibility,
                       LocalDateTime createdAt,
                       LocalDateTime updatedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.persona = persona;
        this.visibility = visibility;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    Long id() {
        return id;
    }

    Long ownerId() {
        return ownerId;
    }

    Long categoryId() {
        return categoryId;
    }

    String name() {
        return name;
    }

    String description() {
        return description;
    }

    String persona() {
        return persona;
    }

    String visibility() {
        return visibility;
    }

    LocalDateTime createdAt() {
        return createdAt;
    }

    LocalDateTime updatedAt() {
        return updatedAt;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String personality;

    @Column(name = "speech_style", columnDefinition = "TEXT")
    private String speechStyle;

    @Column(columnDefinition = "TEXT")
    private String persona;

    @Column(nullable = false)
    private String visibility;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
