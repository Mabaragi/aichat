package com.example.aichat.character.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for partially updating a character.")
public record UpdateCharacterRequest(
        @Schema(description = "Updated character name.", example = "Rational Gourmet")
        @Size(max = 50) String name,
        @Schema(description = "Updated character category slug.", example = "critic")
        String category,
        @Schema(description = "Updated character description.",
                example = "A calm debater who analyzes food choices logically.")
        @Size(max = 1000) String description,
        @Schema(description = "Updated structured persona rules used during debate.")
        @Valid PersonaPayload persona,
        @Schema(description = "Updated character visibility.",
                allowableValues = {"PUBLIC", "PRIVATE"},
                example = "PUBLIC")
        String visibility
) {
}
