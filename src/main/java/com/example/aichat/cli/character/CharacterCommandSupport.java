package com.example.aichat.cli.character;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

final class CharacterCommandSupport {

    private CharacterCommandSupport() {
    }

    static String normalizeJson(ObjectMapper objectMapper, String rawJson, String optionName) {
        if (rawJson == null) {
            return null;
        }

        try {
            JsonNode node = objectMapper.readTree(rawJson);
            return node.toString();
        } catch (Exception exception) {
            throw new IllegalArgumentException(optionName + " must be valid JSON");
        }
    }
}
