package com.example.aichat.debate.web;

import com.example.aichat.debate.domain.DebateFormat;
import com.example.aichat.debate.domain.ParticipantModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateDebateSessionRequest(
        @NotNull Long ownerId,
        @NotNull @Valid TopicRequest topic,
        @NotNull DebateFormat format,
        @Min(1) @Max(10) int maxRounds,
        @NotNull @Min(100) @Max(2000) Integer maxTurnLength,
        @NotNull @Size(min = 2, max = 2) List<@Valid ParticipantRequest> participants
) {

    public record TopicRequest(
            @NotBlank String title,
            @NotBlank String description,
            String category
    ) {
    }

    public record ParticipantRequest(
            @NotNull Long characterId,
            @NotNull ParticipantModel model
    ) {
    }
}