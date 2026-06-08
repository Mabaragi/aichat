package com.example.aichat.character.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpeechStyleTest {

    @Test
    void wrapsJsonTextAndSupportsEquality() {
        SpeechStyle first = SpeechStyle.of("{\"tone\":\"차분함\"}");
        SpeechStyle second = SpeechStyle.of("{\"tone\":\"차분함\"}");

        assertThat(first).isEqualTo(second);
        assertThat(first.value()).isEqualTo("{\"tone\":\"차분함\"}");
    }

    @Test
    void rejectBlankOrNullValues() {
        assertThatThrownBy(() -> SpeechStyle.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("speechStyle is required");

        assertThatThrownBy(() -> SpeechStyle.of("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("speechStyle is required");
    }
}
