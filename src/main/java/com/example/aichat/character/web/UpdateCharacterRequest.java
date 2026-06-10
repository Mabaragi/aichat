package com.example.aichat.character.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.JsonNode;

@Schema(description = "Request payload for partially updating a character.")
public record UpdateCharacterRequest(
        @Schema(description = "Updated character name.", example = "Rational Gourmet")
        @Size(max = 50) String name,
        @Schema(description = "Updated character description.",
                example = "A calm debater who analyzes food choices logically.")
        @Size(max = 1000) String description,
        @Schema(description = "Updated personality traits object.",
                type = "object",
                example = "{\"empathy\":80}")
        JsonNode personality,
        @Schema(description = "Updated speech style object.",
                type = "object",
                example = "{\"tone\":\"formal\"}")
        JsonNode speechStyle,
        @Schema(description = "Updated character visibility.",
                allowableValues = {"PUBLIC", "PRIVATE"},
                example = "PUBLIC")
        String visibility
) {
}
