package com.example.aichat.debate.infrastructure;

import com.example.aichat.debate.domain.DebateFormat;
import com.example.aichat.debate.domain.DebateSessionStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "debate_sessions")
public class DebateSessionJpaEntity {

    @Id
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "topic_title", nullable = false)
    private String topicTitle;

    @Column(name = "topic_description", columnDefinition = "TEXT")
    private String topicDescription;

    @Column(name = "topic_category")
    private String topicCategory;

    @Column(nullable = false)
    private String visibility;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DebateSessionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DebateFormat format;

    @Column(name = "max_rounds", nullable = false)
    private int maxRounds;

    @Column(name = "current_round", nullable = false)
    private int currentRound;

    @Column(name = "max_turn_length", nullable = false)
    private Integer maxTurnLength;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<DebateParticipantJpaEntity> participants = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    protected DebateSessionJpaEntity() {
    }

    DebateSessionJpaEntity(Long id, Long ownerId, String topicTitle,
                           String topicDescription, String topicCategory,
                           Long categoryId, String visibility,
                           DebateSessionStatus status, DebateFormat format,
                           int maxRounds, int currentRound, Integer maxTurnLength,
                           LocalDateTime createdAt, LocalDateTime startedAt,
                           LocalDateTime endedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.categoryId = categoryId;
        this.topicTitle = topicTitle;
        this.topicDescription = topicDescription;
        this.topicCategory = topicCategory;
        this.visibility = visibility;
        this.status = status;
        this.format = format;
        this.maxRounds = maxRounds;
        this.currentRound = currentRound;
        this.maxTurnLength = maxTurnLength;
        this.createdAt = createdAt;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    void addParticipant(DebateParticipantJpaEntity participant) {
        participants.add(participant);
        participant.attachTo(this);
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

    String topicTitle() {
        return topicTitle;
    }

    String topicDescription() {
        return topicDescription;
    }

    String topicCategory() {
        return topicCategory;
    }

    String visibility() {
        return visibility;
    }

    DebateSessionStatus status() {
        return status;
    }

    DebateFormat format() {
        return format;
    }

    int maxRounds() {
        return maxRounds;
    }

    int currentRound() {
        return currentRound;
    }

    Integer maxTurnLength() {
        return maxTurnLength;
    }

    List<DebateParticipantJpaEntity> participants() {
        return participants;
    }

    LocalDateTime createdAt() {
        return createdAt;
    }

    LocalDateTime startedAt() {
        return startedAt;
    }

    LocalDateTime endedAt() {
        return endedAt;
    }
}
