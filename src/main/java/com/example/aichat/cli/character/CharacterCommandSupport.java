package com.example.aichat.cli.character;

import com.example.aichat.character.domain.Personality;
import com.example.aichat.character.domain.SpeechStyle;
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

    static Personality toPersonality(ObjectMapper objectMapper, String rawJson) {
        String normalizedJson = normalizeJson(objectMapper, rawJson, "--personality");
        return normalizedJson == null ? null : Personality.of(normalizedJson);
    }

    static SpeechStyle toSpeechStyle(ObjectMapper objectMapper, String rawJson) {
        String normalizedJson = normalizeJson(objectMapper, rawJson, "--speech-style");
        return normalizedJson == null ? null : SpeechStyle.of(normalizedJson);
    }
}
