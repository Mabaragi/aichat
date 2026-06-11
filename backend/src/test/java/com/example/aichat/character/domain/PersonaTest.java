package com.example.aichat.character.domain;

import com.example.aichat.support.PersonaFixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersonaTest {

    @Test
    void createPersonaWithRequiredStructuredFields() {
        Persona first = PersonaFixtures.rationalGourmet();
        Persona second = Persona.of(PersonaFixtures.RATIONAL_GOURMET_JSON);

        assertThat(first).isEqualTo(second);
        assertThat(first.value())
                .contains("\"identity\":\"합리주의 미식가\"")
                .contains("\"coreValues\":[\"실증성\",\"논리\"]")
                .contains("\"mustNotDo\":[\"인신공격하지 않는다.\",\"출처 없는 수치를 만들지 않는다.\"]");
    }

    @Test
    void rejectBlankPersona() {
        assertThatThrownBy(() -> Persona.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("persona is required");

        assertThatThrownBy(() -> Persona.of("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("persona is required");
    }

    @Test
    void rejectEmptyCoreValuesAndMustNotDo() {
        assertThatThrownBy(() -> Persona.of(PersonaFixtures.RATIONAL_GOURMET_JSON
                .replace("\"coreValues\": [\"실증성\", \"논리\"]", "\"coreValues\": []")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("persona.coreValues must not be empty");

        assertThatThrownBy(() -> Persona.of(PersonaFixtures.RATIONAL_GOURMET_JSON
                .replace("\"mustNotDo\": [\"인신공격하지 않는다.\", \"출처 없는 수치를 만들지 않는다.\"]",
                        "\"mustNotDo\": []")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("persona.mustNotDo must not be empty");
    }
}
