package com.example.aichat.character.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public record Persona(String value) {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public Persona {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("persona is required");
        }
        value = normalize(value);
        validate(value);
    }

    public static Persona of(String value) {
        return new Persona(value);
    }

    private static String normalize(String value) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(value);
            return OBJECT_MAPPER.writeValueAsString(root);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("persona must be valid JSON object", exception);
        }
    }

    private static void validate(String value) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(value);
            requireObject(root, "persona");
            requireText(root, "identity");
            requireText(root, "debateRole");
            requireText(root, "defaultStance");
            requireText(root, "evidenceStyle");
            requireArray(root, "coreValues", true);
            requireArray(root, "expertise", false);
            requireArray(root, "debateBehavior", false);
            requireArray(root, "exampleLines", false);

            JsonNode voiceStyle = requireObject(root.get("voiceStyle"), "voiceStyle");
            requireText(voiceStyle, "tone");
            requireText(voiceStyle, "sentenceLength");
            requireText(voiceStyle, "rhetoricalStyle");
            requireArray(voiceStyle, "signaturePhrases", false);

            JsonNode boundaries = requireObject(root.get("boundaries"), "boundaries");
            requireArray(boundaries, "mustDo", true);
            requireArray(boundaries, "mustNotDo", true);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("persona must be valid JSON object", exception);
        }
    }

    private static JsonNode requireObject(JsonNode node, String fieldName) {
        if (node == null || !node.isObject()) {
            throw new IllegalArgumentException(fieldName + " must be an object");
        }
        return node;
    }

    private static String requireText(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || !value.isTextual() || value.asText().isBlank()) {
            throw new IllegalArgumentException("persona." + fieldName + " is required");
        }
        return value.asText();
    }

    private static void requireArray(JsonNode node, String fieldName, boolean requireNonEmpty) {
        JsonNode value = node.get(fieldName);
        if (value == null || !value.isArray()) {
            throw new IllegalArgumentException("persona." + fieldName + " must be an array");
        }
        if (requireNonEmpty && value.isEmpty()) {
            throw new IllegalArgumentException("persona." + fieldName + " must not be empty");
        }
        value.forEach(item -> {
            if (!item.isTextual() || item.asText().isBlank()) {
                throw new IllegalArgumentException("persona." + fieldName + " must contain non-blank strings");
            }
        });
    }
}
