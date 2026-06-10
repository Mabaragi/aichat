package com.example.aichat.character.domain;

public record Personality(String value) {

    public Personality {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("personality is required");
        }

        value = value.trim();
    }

    public static Personality of(String value) {
        return new Personality(value);
    }
}
