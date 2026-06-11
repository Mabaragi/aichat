package com.example.aichat.common.domain;

import java.util.Locale;

public enum Visibility {
    PUBLIC,
    PRIVATE;

    public static final String DEFAULT = "PRIVATE";

    public static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        try {
            return Visibility.valueOf(normalized).name();
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("visibility must be PUBLIC or PRIVATE");
        }
    }
}
