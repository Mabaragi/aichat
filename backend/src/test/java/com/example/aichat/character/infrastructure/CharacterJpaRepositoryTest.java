package com.example.aichat.character.infrastructure;

import com.example.aichat.character.domain.CharacterRepository;
import com.example.aichat.support.PersonaFixtures;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaCharacterRepositoryAdapter.class)
class CharacterJpaRepositoryTest extends CharacterRepositoryContractTest {

    private static final LocalDateTime DEFAULT_TIME = LocalDateTime.of(2026, 6, 8, 12, 0);

    @Autowired
    private CharacterRepository repository;

    @Autowired
    private CharacterJpaRepository characterJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private DataSource dataSource;

    @Override
    protected CharacterRepository repository() {
        return repository;
    }

    @BeforeEach
    void cleanDatabase() {
        characterJpaRepository.deleteAll();
        characterJpaRepository.flush();
        entityManager.clear();
    }

    @Test
    void schemaIncludesExpectedCharacterColumns() {
        assertThat(columnNames()).containsExactlyInAnyOrder(
                "id",
                "owner_id",
                "category_id",
                "name",
                "description",
                "personality",
                "speech_style",
                "persona",
                "visibility",
                "created_at",
                "updated_at"
        );
    }

    @Test
    void savePersistsAndRoundTripsAllMappedFields() {
        CharacterJpaEntity saved = characterJpaRepository.saveAndFlush(sampleEntity());
        Long savedId = CharacterJpaEntityFields.id(saved);

        entityManager.clear();

        assertThat(savedId).isNotNull();
        assertThat(characterJpaRepository.findById(savedId))
                .hasValueSatisfying(entity -> {
                    assertThat(CharacterJpaEntityFields.id(entity)).isEqualTo(savedId);
                    assertThat(CharacterJpaEntityFields.ownerId(entity)).isEqualTo(1L);
                    assertThat(CharacterJpaEntityFields.name(entity)).isEqualTo("합리주의 미식가");
                    assertThat(CharacterJpaEntityFields.description(entity)).isEqualTo("논리적이고 차분하게 음식 취향을 분석하는 캐릭터");
                    assertThat(CharacterJpaEntityFields.persona(entity)).isEqualTo(PersonaFixtures.rationalGourmetJson());
                    assertThat(CharacterJpaEntityFields.visibility(entity)).isEqualTo("PRIVATE");
                    assertThat(CharacterJpaEntityFields.createdAt(entity)).isEqualTo(DEFAULT_TIME);
                    assertThat(CharacterJpaEntityFields.updatedAt(entity)).isEqualTo(DEFAULT_TIME);
                });
    }

    @Test
    void findByOwnerIdReturnsOnlyRequestedOwnersCharacters() {
        CharacterJpaEntity ownerOneFirst = characterJpaRepository.saveAndFlush(sampleEntity());
        CharacterJpaEntity ownerTwo = characterJpaRepository.saveAndFlush(
                sampleEntity(
                        null,
                        2L,
                        "다른 주인",
                        "다른 설명",
                        PersonaFixtures.rationalGourmetJson(),
                        "PRIVATE",
                        DEFAULT_TIME,
                        DEFAULT_TIME
                )
        );
        CharacterJpaEntity ownerOneSecond = characterJpaRepository.saveAndFlush(
                sampleEntity(
                        null,
                        1L,
                        "두 번째",
                        null,
                        PersonaFixtures.empatheticJson(),
                        "PUBLIC",
                        DEFAULT_TIME,
                        DEFAULT_TIME
                )
        );

        entityManager.clear();

        List<CharacterJpaEntity> ownerOneCharacters = characterJpaRepository.findByOwnerId(1L);

        assertThat(ownerOneCharacters)
                .hasSize(2)
                .allSatisfy(entity -> assertThat(CharacterJpaEntityFields.ownerId(entity)).isEqualTo(1L))
                .extracting(CharacterJpaEntityFields::name)
                .containsExactlyInAnyOrder("합리주의 미식가", "두 번째");

        assertThat(characterJpaRepository.findByOwnerId(99L)).isEmpty();
        assertThat(CharacterJpaEntityFields.ownerId(ownerTwo)).isEqualTo(2L);
        assertThat(CharacterJpaEntityFields.id(ownerOneFirst)).isNotEqualTo(CharacterJpaEntityFields.id(ownerOneSecond));
    }

    @Test
    void findPublicFiltersByVisibilityQueryAndCategory() {
        Long expertCategoryId = categoryId("CHARACTER", "expert");
        Long utilityCategoryId = categoryId("CHARACTER", "utility");

        characterJpaRepository.saveAndFlush(sampleEntity(
                null,
                1L,
                        expertCategoryId,
                        "공개 미식가",
                        "탕수육 취향을 분석한다",
                PersonaFixtures.rationalGourmetJson(),
                "PUBLIC",
                DEFAULT_TIME,
                DEFAULT_TIME
        ));
        characterJpaRepository.saveAndFlush(sampleEntity(
                null,
                1L,
                        utilityCategoryId,
                        "공개 도우미",
                        "일정을 정리한다",
                PersonaFixtures.empatheticJson(),
                "PUBLIC",
                DEFAULT_TIME,
                DEFAULT_TIME
        ));
        characterJpaRepository.saveAndFlush(sampleEntity(
                null,
                2L,
                        expertCategoryId,
                        "비공개 미식가",
                        "검색되어서는 안 된다",
                PersonaFixtures.rationalGourmetJson(),
                "PRIVATE",
                DEFAULT_TIME,
                DEFAULT_TIME
        ));

        var result = repository.findPublic("미식가", expertCategoryId, 0, 20);

        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.items())
                .extracting(com.example.aichat.character.domain.Character::getName)
                .containsExactly("공개 미식가");
    }

    @Test
    void saveWithExistingIdUpdatesPersistedRow() {
        CharacterJpaEntity saved = characterJpaRepository.saveAndFlush(sampleEntity());
        Long savedId = CharacterJpaEntityFields.id(saved);

        CharacterJpaEntity updated = sampleEntity(
                savedId,
                1L,
                "새 이름",
                null,
                PersonaFixtures.empatheticJson(),
                "PUBLIC",
                DEFAULT_TIME,
                DEFAULT_TIME.plusHours(1)
        );

        CharacterJpaEntity reSaved = characterJpaRepository.saveAndFlush(updated);

        entityManager.clear();

        assertThat(CharacterJpaEntityFields.id(reSaved)).isEqualTo(savedId);
        assertThat(characterJpaRepository.findById(savedId))
                .hasValueSatisfying(entity -> {
                    assertThat(CharacterJpaEntityFields.id(entity)).isEqualTo(savedId);
                    assertThat(CharacterJpaEntityFields.ownerId(entity)).isEqualTo(1L);
                    assertThat(CharacterJpaEntityFields.name(entity)).isEqualTo("새 이름");
                    assertThat(CharacterJpaEntityFields.description(entity)).isNull();
                    assertThat(CharacterJpaEntityFields.persona(entity)).isEqualTo(PersonaFixtures.empatheticJson());
                    assertThat(CharacterJpaEntityFields.visibility(entity)).isEqualTo("PUBLIC");
                    assertThat(CharacterJpaEntityFields.createdAt(entity)).isEqualTo(DEFAULT_TIME);
                    assertThat(CharacterJpaEntityFields.updatedAt(entity)).isEqualTo(DEFAULT_TIME.plusHours(1));
                });
    }

    @Test
    void deleteByIdRemovesPersistedRow() {
        CharacterJpaEntity saved = characterJpaRepository.saveAndFlush(sampleEntity());
        Long savedId = CharacterJpaEntityFields.id(saved);

        characterJpaRepository.deleteById(savedId);
        characterJpaRepository.flush();

        entityManager.clear();

        assertThat(characterJpaRepository.findById(savedId)).isEmpty();
        assertThat(characterJpaRepository.findByOwnerId(1L)).isEmpty();
    }

    private List<String> columnNames() {
        return new JdbcTemplate(dataSource)
                .queryForList("PRAGMA table_info(characters)")
                .stream()
                .map(row -> (String) row.get("name"))
                .toList();
    }

    private Long categoryId(String scope, String slug) {
        return new JdbcTemplate(dataSource)
                .queryForObject(
                        "select id from categories where scope = ? and slug = ?",
                        Long.class,
                        scope,
                        slug
                );
    }

    private static CharacterJpaEntity sampleEntity() {
        return sampleEntity(
                null,
                1L,
                null,
                "합리주의 미식가",
                "논리적이고 차분하게 음식 취향을 분석하는 캐릭터",
                PersonaFixtures.rationalGourmetJson(),
                "PRIVATE",
                DEFAULT_TIME,
                DEFAULT_TIME
        );
    }

    private static CharacterJpaEntity sampleEntity(Long id,
                                                   Long ownerId,
                                                   String name,
                                                   String description,
                                                   String persona,
                                                   String visibility,
                                                   LocalDateTime createdAt,
                                                   LocalDateTime updatedAt) {
        return sampleEntity(
                id,
                ownerId,
                null,
                name,
                description,
                persona,
                visibility,
                createdAt,
                updatedAt
        );
    }

    private static CharacterJpaEntity sampleEntity(Long id,
                                                   Long ownerId,
                                                   Long categoryId,
                                                   String name,
                                                   String description,
                                                   String persona,
                                                   String visibility,
                                                   LocalDateTime createdAt,
                                                   LocalDateTime updatedAt) {
        CharacterJpaEntity entity = new CharacterJpaEntity();
        CharacterJpaEntityFields.set(entity, "id", id);
        CharacterJpaEntityFields.set(entity, "ownerId", ownerId);
        CharacterJpaEntityFields.set(entity, "categoryId", categoryId);
        CharacterJpaEntityFields.set(entity, "name", name);
        CharacterJpaEntityFields.set(entity, "description", description);
        CharacterJpaEntityFields.set(entity, "persona", persona);
        CharacterJpaEntityFields.set(entity, "visibility", visibility);
        CharacterJpaEntityFields.set(entity, "createdAt", createdAt);
        CharacterJpaEntityFields.set(entity, "updatedAt", updatedAt);
        return entity;
    }

    private static final class CharacterJpaEntityFields {

        private CharacterJpaEntityFields() {
        }

        private static Long id(CharacterJpaEntity entity) {
            return field(entity, "id", Long.class);
        }

        private static Long ownerId(CharacterJpaEntity entity) {
            return field(entity, "ownerId", Long.class);
        }

        private static String name(CharacterJpaEntity entity) {
            return field(entity, "name", String.class);
        }

        private static String description(CharacterJpaEntity entity) {
            return field(entity, "description", String.class);
        }

        private static String persona(CharacterJpaEntity entity) {
            return field(entity, "persona", String.class);
        }

        private static String visibility(CharacterJpaEntity entity) {
            return field(entity, "visibility", String.class);
        }

        private static LocalDateTime createdAt(CharacterJpaEntity entity) {
            return field(entity, "createdAt", LocalDateTime.class);
        }

        private static LocalDateTime updatedAt(CharacterJpaEntity entity) {
            return field(entity, "updatedAt", LocalDateTime.class);
        }

        private static <T> T field(CharacterJpaEntity entity, String fieldName, Class<T> type) {
            try {
                Field field = CharacterJpaEntity.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                return type.cast(field.get(entity));
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to read CharacterJpaEntity field: " + fieldName, e);
            }
        }

        private static void set(CharacterJpaEntity entity, String fieldName, Object value) {
            try {
                Field field = CharacterJpaEntity.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(entity, value);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to write CharacterJpaEntity field: " + fieldName, e);
            }
        }
    }
}
