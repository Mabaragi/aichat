package com.example.aichat.debate.infrastructure;

import com.example.aichat.debate.domain.TurnStatus;
import com.example.aichat.debate.domain.TurnType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "debate_turns",
        indexes = @Index(
                name = "idx_debate_turn_session_index",
                columnList = "session_id, turn_index"
        ),
        uniqueConstraints = @UniqueConstraint(
                name = "uk_debate_turn_session_index",
                columnNames = {"session_id", "turn_index"}
        )
)
public class DebateTurnJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private DebateSessionJpaEntity session;

    @Column(name = "participant_id", nullable = false)
    private Long participantId;

    @Column(nullable = false)
    private int round;

    @Column(name = "turn_index", nullable = false)
    private int turnIndex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TurnType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TurnStatus status;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "prompt_snapshot", columnDefinition = "TEXT")
    private String promptSnapshot;

    @Column(name = "model_name")
    private String modelName;

    @Column(name = "input_tokens")
    private Integer inputTokens;

    @Column(name = "output_tokens")
    private Integer outputTokens;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected DebateTurnJpaEntity() {
    }

    DebateTurnJpaEntity(Integer id, Long participantId, int round, int turnIndex,
                        TurnType type, TurnStatus status, String content,
                        String promptSnapshot, String modelName, Integer inputTokens,
                        Integer outputTokens, LocalDateTime createdAt) {
        this.id = id;
        this.participantId = participantId;
        this.round = round;
        this.turnIndex = turnIndex;
        this.type = type;
        this.status = status;
        this.content = content;
        this.promptSnapshot = promptSnapshot;
        this.modelName = modelName;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.createdAt = createdAt;
    }

    void attachTo(DebateSessionJpaEntity session) {
        this.session = session;
    }

    Long sessionId() {
        return session == null ? null : session.id();
    }

    Integer id() {
        return id;
    }

    Long participantId() {
        return participantId;
    }

    int round() {
        return round;
    }

    int turnIndex() {
        return turnIndex;
    }

    TurnType type() {
        return type;
    }

    TurnStatus status() {
        return status;
    }

    String content() {
        return content;
    }

    String promptSnapshot() {
        return promptSnapshot;
    }

    String modelName() {
        return modelName;
    }

    Integer inputTokens() {
        return inputTokens;
    }

    Integer outputTokens() {
        return outputTokens;
    }

    LocalDateTime createdAt() {
        return createdAt;
    }
}
