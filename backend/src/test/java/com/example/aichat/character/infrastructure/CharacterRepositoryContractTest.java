package com.example.aichat.character.infrastructure;

import com.example.aichat.character.domain.Character;
import com.example.aichat.character.domain.CharacterRepository;
import com.example.aichat.support.PersonaFixtures;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

abstract class CharacterRepositoryContractTest {

    protected abstract CharacterRepository repository();

    @Test
    void saveNewCharacterAssignsIdAndRoundTripsAllFields() {
        Character saved = repository().save(sampleCharacter());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getOwnerId()).isEqualTo(1L);
        assertThat(saved.getName()).isEqualTo("합리주의 미식가");
        assertThat(saved.getDescription()).isEqualTo("논리적이고 차분하게 음식 취향을 분석하는 캐릭터");
        assertThat(saved.getPersona()).isEqualTo(PersonaFixtures.rationalGourmet());
        assertThat(saved.getVisibility()).isEqualTo("PRIVATE");
        assertThat(saved.getCreatedAt()).isEqualTo(defaultNow());
        assertThat(saved.getUpdatedAt()).isEqualTo(defaultNow());

        assertThat(repository().findById(saved.getId()))
                .hasValueSatisfying(character -> {
                    assertThat(character.getId()).isEqualTo(saved.getId());
                    assertThat(character.getOwnerId()).isEqualTo(1L);
                    assertThat(character.getName()).isEqualTo("합리주의 미식가");
                    assertThat(character.getDescription()).isEqualTo("논리적이고 차분하게 음식 취향을 분석하는 캐릭터");
                    assertThat(character.getPersona()).isEqualTo(PersonaFixtures.rationalGourmet());
                    assertThat(character.getVisibility()).isEqualTo("PRIVATE");
                    assertThat(character.getCreatedAt()).isEqualTo(defaultNow());
                    assertThat(character.getUpdatedAt()).isEqualTo(defaultNow());
                });
    }

    @Test
    void findByOwnerIdReturnsOnlyRequestedOwnersCharacters() {
        Character ownerOneFirst = repository().save(sampleCharacter());
        Character ownerTwo = repository().save(new Character(
                null,
                2L,
                "다른 주인",
                "다른 설명",
                PersonaFixtures.rationalGourmet(),
                null,
                defaultNow(),
                defaultNow()
        ));
        Character ownerOneSecond = repository().save(new Character(
                null,
                1L,
                "두 번째",
                null,
                PersonaFixtures.empathetic(),
                "PUBLIC",
                defaultNow(),
                defaultNow()
        ));

        List<Character> characters = repository().findByOwnerId(1L);

        assertThat(characters)
                .hasSize(2)
                .allSatisfy(character -> assertThat(character.getOwnerId()).isEqualTo(1L))
                .extracting(Character::getId)
                .containsExactlyInAnyOrder(ownerOneFirst.getId(), ownerOneSecond.getId());

        assertThat(ownerTwo.getOwnerId()).isEqualTo(2L);
        assertThat(repository().findByOwnerId(99L)).isEmpty();
    }

    @Test
    void saveExistingCharacterUpdatesPersistedState() {
        Character saved = repository().save(sampleCharacter());

        Character updated = new Character(
                saved.getId(),
                saved.getOwnerId(),
                "새 이름",
                null,
                PersonaFixtures.empathetic(),
                "PUBLIC",
                saved.getCreatedAt(),
                saved.getUpdatedAt().plusHours(1)
        );

        Character reSaved = repository().save(updated);

        assertThat(reSaved.getId()).isEqualTo(saved.getId());
        assertThat(repository().findById(saved.getId()))
                .hasValueSatisfying(character -> {
                    assertThat(character.getId()).isEqualTo(saved.getId());
                    assertThat(character.getOwnerId()).isEqualTo(1L);
                    assertThat(character.getName()).isEqualTo("새 이름");
                    assertThat(character.getDescription()).isNull();
                    assertThat(character.getPersona()).isEqualTo(PersonaFixtures.empathetic());
                    assertThat(character.getVisibility()).isEqualTo("PUBLIC");
                    assertThat(character.getCreatedAt()).isEqualTo(saved.getCreatedAt());
                    assertThat(character.getUpdatedAt()).isEqualTo(saved.getUpdatedAt().plusHours(1));
                });
    }

    @Test
    void deleteByIdRemovesPersistedCharacter() {
        Character saved = repository().save(sampleCharacter());

        repository().deleteById(saved.getId());

        assertThat(repository().findById(saved.getId())).isEmpty();
        assertThat(repository().findByOwnerId(saved.getOwnerId())).isEmpty();
    }

    private static Character sampleCharacter() {
        return Character.create(
                1L,
                "합리주의 미식가",
                "논리적이고 차분하게 음식 취향을 분석하는 캐릭터",
                PersonaFixtures.rationalGourmet(),
                defaultNow(),
                defaultNow()
        );
    }

    private static LocalDateTime defaultNow() {
        return LocalDateTime.of(2026, 6, 8, 12, 0);
    }
}
