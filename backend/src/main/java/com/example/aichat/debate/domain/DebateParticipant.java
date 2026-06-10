package com.example.aichat.debate.domain;

import lombok.Getter;

@Getter
public class DebateParticipant {

    private final Long id;
    private final Long sourceCharacterId;
    private final int position;
    private final ParticipantModel model;
    private final String name;
    private final String description;
    private final String personality;
    private final String speechStyle;

    public DebateParticipant(Long id, Long sourceCharacterId, int position,
                             ParticipantModel model, String name, String description,
                             String personality, String speechStyle) {
        validateSourceCharacterId(sourceCharacterId);
        validatePosition(position);
        validateModel(model);
        validateName(name);

        this.id = id;
        this.sourceCharacterId = sourceCharacterId;
        this.position = position;
        this.model = model;
        this.name = name;
        this.description = description;
        this.personality = personality;
        this.speechStyle = speechStyle;
    }

    public static DebateParticipant create(Long sourceCharacterId, int position,
                                            ParticipantModel model, String name,
                                            String description, String personality,
                                            String speechStyle) {
        return new DebateParticipant(
                null,
                sourceCharacterId,
                position,
                model,
                name,
                description,
                personality,
                speechStyle
        );
    }

    private static void validateSourceCharacterId(Long sourceCharacterId) {
        if (sourceCharacterId == null) {
            throw new IllegalArgumentException("sourceCharacterId is required");
        }
    }

    private static void validatePosition(int position) {
        if (position < 0 || position >= DebateSession.REQUIRED_PARTICIPANT_COUNT) {
            throw new IllegalArgumentException("position must be 0 or 1");
        }
    }

    private static void validateModel(ParticipantModel model) {
        if (model == null) {
            throw new IllegalArgumentException("model is required");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
    }
}
