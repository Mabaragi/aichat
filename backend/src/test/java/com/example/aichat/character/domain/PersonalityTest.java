package com.example.aichat.character.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersonalityTest {

    @Test
    void wrapsJsonTextAndSupportsEquality() {
        Personality first = Personality.of("{\"rationality\":90}");
        Personality second = Personality.of("{\"rationality\":90}");

        assertThat(first).isEqualTo(second);
        assertThat(first.value()).isEqualTo("{\"rationality\":90}");
    }

    @Test
    void rejectBlankOrNullValues() {
        assertThatThrownBy(() -> Personality.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("personality is required");

        assertThatThrownBy(() -> Personality.of("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("personality is required");
    }
}
