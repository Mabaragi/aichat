package com.example.aichat.character.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.JsonNode;

@Schema(description = "Request payload for creating a character.")
public record CreateCharacterRequest(
        @Schema(description = "Character name.", example = "Rational Gourmet")
        @NotBlank @Size(max = 50) String name,
        @Schema(description = "Short character description.",
                example = "A calm debater who analyzes food choices logically.")
        @Size(max = 1000) String description,
        @Schema(description = "Arbitrary personality traits object.",
                type = "object",
                example = "{\"rationality\":90,\"humor\":30}")
        JsonNode personality,
        @Schema(description = "Arbitrary speech style object.",
                type = "object",
                example = "{\"tone\":\"calm\",\"formality\":\"high\"}")
        JsonNode speechStyle,
        @Schema(description = "Character visibility. Defaults to PRIVATE when omitted.",
                allowableValues = {"PUBLIC", "PRIVATE"},
                example = "PRIVATE")
        String visibility
) {
}
