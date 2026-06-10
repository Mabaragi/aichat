package com.example.aichat.user.infrastructure;

import com.example.aichat.user.domain.User;
import com.example.aichat.user.domain.UserRepository;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaUserRepositoryAdapter.class)
class UserJpaRepositoryTest {

    private static final LocalDateTime DEFAULT_TIME = LocalDateTime.of(2026, 6, 5, 12, 0);

    @Autowired
    private UserRepository repository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void cleanDatabase() {
        userJpaRepository.deleteAll();
        userJpaRepository.flush();
        entityManager.clear();
    }

    @Test
    void schemaIncludesExpectedUserColumns() {
        assertThat(columnNames()).containsExactlyInAnyOrder(
                "id",
                "email",
                "password_hash",
                "nickname",
                "created_at"
        );
    }

    @Test
    void savePersistsAndRoundTripsAllMappedFields() {
        User saved = repository.save(sampleUser());

        entityManager.clear();

        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId()))
                .hasValueSatisfying(user -> {
                    assertThat(user.getId()).isEqualTo(saved.getId());
                    assertThat(user.getEmail()).isEqualTo("user@example.com");
                    assertThat(user.getPasswordHash()).isEqualTo("{bcrypt}hash");
                    assertThat(user.getNickname()).isEqualTo("마바라기");
                    assertThat(user.getCreatedAt()).isEqualTo(DEFAULT_TIME);
                });
    }

    private List<String> columnNames() {
        return new JdbcTemplate(dataSource)
                .queryForList("PRAGMA table_info(users)")
                .stream()
                .map(row -> (String) row.get("name"))
                .toList();
    }

    private static User sampleUser() {
        return User.create("user@example.com", "{bcrypt}hash", "마바라기", DEFAULT_TIME);
    }
}
