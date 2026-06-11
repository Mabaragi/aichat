package com.example.aichat.debate.web;

import com.example.aichat.debate.domain.DebateFormat;
import com.example.aichat.debate.domain.ParticipantModel;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Request payload for creating a debate session.")
public record CreateDebateSessionRequest(
        @Schema(description = "Debate topic metadata.")
        @NotNull @Valid TopicRequest topic,
        @Schema(description = "Debate format.", example = "PROS_AND_CONS")
        @NotNull DebateFormat format,
        @Schema(description = "Debate visibility. PUBLIC sessions appear in the public catalog after completion.",
                allowableValues = {"PUBLIC", "PRIVATE"},
                example = "PUBLIC")
        String visibility,
        @Schema(description = "Maximum number of rounds.", example = "5")
        @Min(1) @Max(10) int maxRounds,
        @Schema(description = "Maximum length of each generated turn in characters.",
                example = "600")
        @NotNull @Min(100) @Max(2000) Integer maxTurnLength,
        @ArraySchema(
                arraySchema = @Schema(description = "Exactly two participants to include.",
                        example = """
                                [
                                  {"characterId": 10, "model": "FAST"},
                                  {"characterId": 20, "model": "QUALITY"}
                                ]
                                """),
                schema = @Schema(implementation = ParticipantRequest.class,
                        description = "Participant selection.")
        )
        @NotNull @Size(min = 2, max = 2) List<@Valid ParticipantRequest> participants
) {

    @Schema(name = "CreateDebateSessionTopicRequest", description = "Debate topic details.")
    public record TopicRequest(
            @Schema(description = "Topic title.", example = "Sauce-first vs dip-first")
            @NotBlank String title,
            @Schema(description = "Topic description.",
                    example = "Which serving style creates the better eating experience?")
            @NotBlank String description,
            @Schema(description = "Topic category slug. Defaults to other when omitted.",
                    example = "food")
            String category
    ) {
    }

    @Schema(name = "CreateDebateSessionParticipantRequest",
            description = "Selected character and model for one debate side.")
    public record ParticipantRequest(
            @Schema(description = "Character identifier.", example = "10")
            @NotNull Long characterId,
            @Schema(description = "Generation model to use for this participant.",
                    example = "FAST")
            @NotNull ParticipantModel model
    ) {
    }
}
