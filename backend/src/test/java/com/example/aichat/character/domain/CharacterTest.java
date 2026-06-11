package com.example.aichat.character.domain;

import com.example.aichat.support.PersonaFixtures;
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
        var persona = PersonaFixtures.rationalGourmet();

        Character character = characterFixture().create();

        assertThat(character.getOwnerId()).isEqualTo(ownerId);
        assertThat(character.getName()).isEqualTo(name);
        assertThat(character.getDescription()).isEqualTo(description);
        assertThat(character.getPersona()).isEqualTo(persona);
        assertThat(character.getCreatedAt()).isEqualTo(defaultNow());
        assertThat(character.getUpdatedAt()).isEqualTo(defaultNow());
        assertThat(character.getVisibility()).isEqualTo("PRIVATE");
    }

    @Test
    void createCharacterWithCustomVisibility() {
        Character character = Character.create(
                1L,
                "홍길동",
                "의적",
                PersonaFixtures.rationalGourmet(),
                "public",
                defaultNow(),
                defaultNow()
        );

        assertThat(character.getVisibility()).isEqualTo("PUBLIC");
    }

    @Test
    void rejectCharacterWithNullOwnerId() {
        var fixture = characterFixture().ownerId(null);

        assertThatThrownBy(fixture::create)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ownerId is required");
    }

    @Test
    void rejectCharacterWithBlankName() {
        var fixture = characterFixture().name("   ");

        assertThatThrownBy(fixture::create)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name is required");
    }

    @Test
    void rejectCharacterWithToolongName() {
        CharacterFixture fixture = characterFixture().name("a".repeat(51));
        assertThatThrownBy(fixture::create).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectCharacterWithToolongDescription() {
        var fixture = characterFixture().description("a".repeat(1001));
        assertThatThrownBy(fixture::create).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void allowCharacterWithNullDescription() {
        Character character = characterFixture().description(null).create();

        assertThat(character.getDescription()).isNull();
    }

    @Test
    void rejectCharacterWithNullPersona() {
        var fixture = characterFixture().persona(null);

        assertThatThrownBy(fixture::create)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("persona is required");
    }

    @Test
    void updateCharacterChangesMutableFields() {
        Character character = characterFixture().create();
        var updatedAt = defaultNow().plusHours(1);

        character.update(
                "새 이름",
                null,
                PersonaFixtures.empathetic(),
                "public",
                updatedAt
        );

        assertThat(character.getName()).isEqualTo("새 이름");
        assertThat(character.getDescription()).isNull();
        assertThat(character.getPersona()).isEqualTo(PersonaFixtures.empathetic());
        assertThat(character.getVisibility()).isEqualTo("PUBLIC");
        assertThat(character.getCreatedAt()).isEqualTo(defaultNow());
        assertThat(character.getUpdatedAt()).isEqualTo(updatedAt);
    }

    private static Persona defaultPersona() {
        return PersonaFixtures.rationalGourmet();
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
        private Persona persona = defaultPersona();
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
            return Character.create(ownerId, name, description, persona,
                    createdAt, updatedAt);
        }

        CharacterFixture persona(Persona persona) {
            this.persona = persona;
            return this;
        }
    }
}
