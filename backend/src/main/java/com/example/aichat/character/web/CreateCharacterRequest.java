package com.example.aichat.character.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;

@Schema(description = "Request payload for creating a character.")
public record CreateCharacterRequest(
        @Schema(description = "Character name.", example = "Rational Gourmet")
        @NotBlank @Size(max = 50) String name,
        @Schema(description = "Character category slug. Defaults to other when omitted.",
                example = "expert")
        String category,
        @Schema(description = "Short character description.",
                example = "A calm debater who analyzes food choices logically.")
        @Size(max = 1000) String description,
        @Schema(description = "Arbitrary personality traits object.",
                example = "{\"rationality\":90,\"humor\":30}")
        Map<String, Object> personality,
        @Schema(description = "Arbitrary speech style object.",
                example = "{\"tone\":\"calm\",\"formality\":\"high\"}")
        Map<String, Object> speechStyle,
        @Schema(description = "Character visibility. Defaults to PRIVATE when omitted.",
                allowableValues = {"PUBLIC", "PRIVATE"},
                example = "PRIVATE")
        String visibility
) {
}
