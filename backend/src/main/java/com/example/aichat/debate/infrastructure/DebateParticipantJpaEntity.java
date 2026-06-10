package com.example.aichat.debate.infrastructure;

import com.example.aichat.debate.domain.ParticipantModel;
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

@Entity
@Table(
        name = "debate_participants",
        indexes = @Index(
                name = "idx_debate_participant_session_position",
                columnList = "session_id, position"
        ),
        uniqueConstraints = @UniqueConstraint(
                name = "uk_debate_participant_session_position",
                columnNames = {"session_id", "position"}
        )
)
public class DebateParticipantJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private DebateSessionJpaEntity session;

    @Column(name = "source_character_id", nullable = false)
    private Long sourceCharacterId;

    @Column(nullable = false)
    private int position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantModel model;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String personality;

    @Column(name = "speech_style", columnDefinition = "TEXT")
    private String speechStyle;

    protected DebateParticipantJpaEntity() {
    }

    DebateParticipantJpaEntity(Integer id, Long sourceCharacterId, int position,
                               ParticipantModel model, String name, String description,
                               String personality, String speechStyle) {
        this.id = id;
        this.sourceCharacterId = sourceCharacterId;
        this.position = position;
        this.model = model;
        this.name = name;
        this.description = description;
        this.personality = personality;
        this.speechStyle = speechStyle;
    }

    void attachTo(DebateSessionJpaEntity session) {
        this.session = session;
    }

    Integer id() {
        return id;
    }

    Long sourceCharacterId() {
        return sourceCharacterId;
    }

    int position() {
        return position;
    }

    ParticipantModel model() {
        return model;
    }

    String name() {
        return name;
    }

    String description() {
        return description;
    }

    String personality() {
        return personality;
    }

    String speechStyle() {
        return speechStyle;
    }
}
