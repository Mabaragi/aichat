package com.example.aichat.character.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.util.Map;

@Schema(description = "Request payload for partially updating a character.")
public record UpdateCharacterRequest(
        @Schema(description = "Updated character name.", example = "Rational Gourmet")
        @Size(max = 50) String name,
        @Schema(description = "Updated character category slug.", example = "critic")
        String category,
        @Schema(description = "Updated character description.",
                example = "A calm debater who analyzes food choices logically.")
        @Size(max = 1000) String description,
        @Schema(description = "Updated personality traits object.",
                example = "{\"empathy\":80}")
        Map<String, Object> personality,
        @Schema(description = "Updated speech style object.",
                example = "{\"tone\":\"formal\"}")
        Map<String, Object> speechStyle,
        @Schema(description = "Updated character visibility.",
                allowableValues = {"PUBLIC", "PRIVATE"},
                example = "PUBLIC")
        String visibility
) {
}
