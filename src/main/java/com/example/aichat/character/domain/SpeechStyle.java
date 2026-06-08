package com.example.aichat.character.domain;

public record SpeechStyle(String value) {

    public SpeechStyle {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("speechStyle is required");
        }

        value = value.trim();
    }

    public static SpeechStyle of(String value) {
        return new SpeechStyle(value);
    }
}
