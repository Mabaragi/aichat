package com.example.aichat.character.web;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Size;

public record UpdateCharacterRequest(
        @Size(max = 50) String name,
        @Size(max = 1000) String description,
        JsonNode personality,
        JsonNode speechStyle,
        String visibility
) {
}
