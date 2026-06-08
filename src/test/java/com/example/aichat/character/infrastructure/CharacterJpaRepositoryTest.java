package com.example.aichat.character.infrastructure;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
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
class CharacterJpaRepositoryTest {

    private static final LocalDateTime DEFAULT_TIME = LocalDateTime.of(2026, 6, 8, 12, 0);

    @Autowired
    private CharacterJpaRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private DataSource dataSource;

    @Test
    void schemaIncludesExpectedCharacterColumns() {
        assertThat(columnNames()).containsExactlyInAnyOrder(
                "id",
                "owner_id",
                "name",
                "description",
                "personality",
                "speech_style",
                "visibility",
                "created_at",
                "updated_at"
        );
    }

    @Test
    void savePersistsAndRoundTripsAllMappedFields() {
        CharacterJpaEntity saved = repository.saveAndFlush(sampleEntity());
        Long savedId = CharacterJpaEntityFields.id(saved);

        entityManager.clear();

        assertThat(savedId).isNotNull();
        assertThat(repository.findById(savedId))
                .hasValueSatisfying(entity -> {
                    assertThat(CharacterJpaEntityFields.id(entity)).isEqualTo(savedId);
                    assertThat(CharacterJpaEntityFields.ownerId(entity)).isEqualTo(1L);
                    assertThat(CharacterJpaEntityFields.name(entity)).isEqualTo("합리주의 미식가");
                    assertThat(CharacterJpaEntityFields.description(entity)).isEqualTo("논리적이고 차분하게 음식 취향을 분석하는 캐릭터");
                    assertThat(CharacterJpaEntityFields.personality(entity)).isEqualTo("{\"rationality\":90}");
                    assertThat(CharacterJpaEntityFields.speechStyle(entity)).isEqualTo("{\"tone\":\"차분함\"}");
                    assertThat(CharacterJpaEntityFields.visibility(entity)).isEqualTo("PRIVATE");
                    assertThat(CharacterJpaEntityFields.createdAt(entity)).isEqualTo(DEFAULT_TIME);
                    assertThat(CharacterJpaEntityFields.updatedAt(entity)).isEqualTo(DEFAULT_TIME);
                });
    }

    @Test
    void findByOwnerIdReturnsOnlyRequestedOwnersCharacters() {
        CharacterJpaEntity ownerOneFirst = repository.saveAndFlush(sampleEntity());
        CharacterJpaEntity ownerTwo = repository.saveAndFlush(
                sampleEntity(
                        null,
                        2L,
                        "다른 주인",
                        "다른 설명",
                        "{\"rationality\":10}",
                        "{\"tone\":\"차분함\"}",
                        "PRIVATE",
                        DEFAULT_TIME,
                        DEFAULT_TIME
                )
        );
        CharacterJpaEntity ownerOneSecond = repository.saveAndFlush(
                sampleEntity(
                        null,
                        1L,
                        "두 번째",
                        null,
                        "{\"humor\":60}",
                        "{\"tone\":\"반말\"}",
                        "PUBLIC",
                        DEFAULT_TIME,
                        DEFAULT_TIME
                )
        );

        entityManager.clear();

        List<CharacterJpaEntity> ownerOneCharacters = repository.findByOwnerId(1L);

        assertThat(ownerOneCharacters)
                .hasSize(2)
                .allSatisfy(entity -> assertThat(CharacterJpaEntityFields.ownerId(entity)).isEqualTo(1L))
                .extracting(CharacterJpaEntityFields::name)
                .containsExactlyInAnyOrder("합리주의 미식가", "두 번째");

        assertThat(repository.findByOwnerId(99L)).isEmpty();
        assertThat(CharacterJpaEntityFields.ownerId(ownerTwo)).isEqualTo(2L);
        assertThat(CharacterJpaEntityFields.id(ownerOneFirst)).isNotEqualTo(CharacterJpaEntityFields.id(ownerOneSecond));
    }

    @Test
    void saveWithExistingIdUpdatesPersistedRow() {
        CharacterJpaEntity saved = repository.saveAndFlush(sampleEntity());
        Long savedId = CharacterJpaEntityFields.id(saved);

        CharacterJpaEntity updated = sampleEntity(
                savedId,
                1L,
                "새 이름",
                null,
                "{\"empathy\":80}",
                "{\"tone\":\"반말\"}",
                "PUBLIC",
                DEFAULT_TIME,
                DEFAULT_TIME.plusHours(1)
        );

        CharacterJpaEntity reSaved = repository.saveAndFlush(updated);

        entityManager.clear();

        assertThat(CharacterJpaEntityFields.id(reSaved)).isEqualTo(savedId);
        assertThat(repository.findById(savedId))
                .hasValueSatisfying(entity -> {
                    assertThat(CharacterJpaEntityFields.id(entity)).isEqualTo(savedId);
                    assertThat(CharacterJpaEntityFields.ownerId(entity)).isEqualTo(1L);
                    assertThat(CharacterJpaEntityFields.name(entity)).isEqualTo("새 이름");
                    assertThat(CharacterJpaEntityFields.description(entity)).isNull();
                    assertThat(CharacterJpaEntityFields.personality(entity)).isEqualTo("{\"empathy\":80}");
                    assertThat(CharacterJpaEntityFields.speechStyle(entity)).isEqualTo("{\"tone\":\"반말\"}");
                    assertThat(CharacterJpaEntityFields.visibility(entity)).isEqualTo("PUBLIC");
                    assertThat(CharacterJpaEntityFields.createdAt(entity)).isEqualTo(DEFAULT_TIME);
                    assertThat(CharacterJpaEntityFields.updatedAt(entity)).isEqualTo(DEFAULT_TIME.plusHours(1));
                });
    }

    @Test
    void deleteByIdRemovesPersistedRow() {
        CharacterJpaEntity saved = repository.saveAndFlush(sampleEntity());
        Long savedId = CharacterJpaEntityFields.id(saved);

        repository.deleteById(savedId);
        repository.flush();

        entityManager.clear();

        assertThat(repository.findById(savedId)).isEmpty();
        assertThat(repository.findByOwnerId(1L)).isEmpty();
    }

    private List<String> columnNames() {
        return new JdbcTemplate(dataSource)
                .queryForList("PRAGMA table_info(characters)")
                .stream()
                .map(row -> (String) row.get("name"))
                .toList();
    }

    private static CharacterJpaEntity sampleEntity() {
        return sampleEntity(
                null,
                1L,
                "합리주의 미식가",
                "논리적이고 차분하게 음식 취향을 분석하는 캐릭터",
                "{\"rationality\":90}",
                "{\"tone\":\"차분함\"}",
                "PRIVATE",
                DEFAULT_TIME,
                DEFAULT_TIME
        );
    }

    private static CharacterJpaEntity sampleEntity(Long id,
                                                   Long ownerId,
                                                   String name,
                                                   String description,
                                                   String personality,
                                                   String speechStyle,
                                                   String visibility,
                                                   LocalDateTime createdAt,
                                                   LocalDateTime updatedAt) {
        CharacterJpaEntity entity = new CharacterJpaEntity();
        CharacterJpaEntityFields.set(entity, "id", id);
        CharacterJpaEntityFields.set(entity, "ownerId", ownerId);
        CharacterJpaEntityFields.set(entity, "name", name);
        CharacterJpaEntityFields.set(entity, "description", description);
        CharacterJpaEntityFields.set(entity, "personality", personality);
        CharacterJpaEntityFields.set(entity, "speechStyle", speechStyle);
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

        private static String personality(CharacterJpaEntity entity) {
            return field(entity, "personality", String.class);
        }

        private static String speechStyle(CharacterJpaEntity entity) {
            return field(entity, "speechStyle", String.class);
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
