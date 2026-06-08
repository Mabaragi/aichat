package com.example.aichat.debate.domain;

import lombok.Getter;

@Getter
public class DebateParticipant {

    private final ParticipantModel model;

    public DebateParticipant(ParticipantModel model) {
        if (model == null) {
            throw new IllegalArgumentException("model is required");
        }
        this.model = model;
    }
}
