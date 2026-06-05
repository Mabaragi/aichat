package com.example.aichat.character.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CharacterTest {


    @Test
    void createCharacterWithRequiredFields() {
        var ownerId = 1L;
        var name = "홍길동";
        var description = "의적";
        var personality = "의연함";
        var speechStyle = "합쇼체";

        Character character = characterFixture().create();

        assertThat(character.getOwnerId()).isEqualTo(1L);
        assertThat(character.getName()).isEqualTo(name);
        assertThat(character.getDescription()).isEqualTo(description);
        assertThat(character.getSpeechStyle()).isEqualTo(speechStyle);
        assertThat(character.getCreatedAt()).isEqualTo(defaultNow());
        assertThat(character.getUpdatedAt()).isEqualTo(defaultNow());
        assertThat(character.getVisibility()).isEqualTo("PRIVATE");
    }

    @Test
    void rejectCharacterWithToolongName() {
        CharacterFixture fixture = characterFixture().name(
                "a".repeat(51));
        assertThatThrownBy(fixture::create).isInstanceOf(IllegalArgumentException.class);
    }
    @Test
    void rejectCharacterWithToolongDescription() {
        var fixture = characterFixture().description("a".repeat(1001));
        assertThatThrownBy(fixture::create).isInstanceOf(IllegalArgumentException.class);
    }


    private static CharacterFixture characterFixture() {
        return new CharacterFixture();
    }

    private static LocalDateTime defaultNow() {
        return LocalDateTime.of(2026, 6, 5, 12, 0);
    }

    private static class CharacterFixture {
        private Long ownerId = 1L;
        private String name = "홍길동";
        private String description = "의적";
        private String personality = "의연함";
        private String speechStyle = "합쇼체";
        private LocalDateTime createdAt = defaultNow();
        private LocalDateTime updatedAt = defaultNow();

        CharacterFixture ownerId(Long ownerId) {
            this.ownerId = ownerId;
            return this;
        }

        CharacterFixture name(String name) {
            this.name = name;
            return this;
        }

        CharacterFixture description(String description) {
            this.description = description;
            return this;
        }


        Character create() {
            return Character.create(ownerId, name, description, personality,
                    speechStyle, createdAt, updatedAt);
        }
    }
}
