package com.example.aichat.debate.infrastructure;

import com.example.aichat.debate.domain.DebateFormat;
import com.example.aichat.debate.domain.DebateParticipant;
import com.example.aichat.debate.domain.DebateSession;
import com.example.aichat.debate.domain.DebateSessionRepository;
import com.example.aichat.debate.domain.DebateSessionStatus;
import com.example.aichat.debate.domain.ParticipantModel;
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
@Import(JpaDebateSessionRepositoryAdapter.class)
class DebateSessionJpaRepositoryTest {

    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2026, 6, 10, 12, 0);

    @Autowired
    private DebateSessionRepository repository;

    @Autowired
    private DebateSessionJpaRepository jpaRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void cleanDatabase() {
        jpaRepository.deleteAll();
        jpaRepository.flush();
        entityManager.clear();
    }

    @Test
    void saveCascadesParticipantsAndRoundTripsSnapshotsInPositionOrder() {
        DebateSession saved = repository.save(session());
        entityManager.flush();
        entityManager.clear();

        DebateSession found = repository.findById(saved.getId()).orElseThrow();

        assertThat(found.getId()).isNotNull();
        assertThat(found.getOwnerId()).isEqualTo(1L);
        assertThat(found.getTopicTitle()).isEqualTo("부먹 vs 찍먹");
        assertThat(found.getParticipants())
                .extracting(DebateParticipant::getPosition)
                .containsExactly(0, 1);
        assertThat(found.getParticipants())
                .extracting(DebateParticipant::getId)
                .doesNotContainNull();
        assertThat(found.getParticipants())
                .extracting(DebateParticipant::getSourceCharacterId)
                .containsExactly(10L, 20L);
        assertThat(found.getParticipants())
                .extracting(DebateParticipant::getName)
                .containsExactly("첫 번째 캐릭터", "두 번째 캐릭터");
        assertThat(found.getParticipants())
                .extracting(DebateParticipant::getPersonality)
                .containsExactly("{\"rationality\":90}", null);
    }

    @Test
    void assignsDistinctSessionIdsAcrossMultipleSaves() {
        DebateSession first = repository.save(session());
        DebateSession second = repository.save(session());

        assertThat(second.getId()).isGreaterThan(first.getId());
    }

    @Test
    void findPublicCompletedReturnsOnlyPublicCompletedSessionsInCategory() {
        Long foodCategoryId = categoryId("DEBATE", "food");
        Long techCategoryId = categoryId("DEBATE", "tech");
        DebateSession visible = repository.save(session(
                foodCategoryId,
                "PUBLIC",
                DebateSessionStatus.COMPLETED,
                "부먹 vs 찍먹",
                FIXED_TIME.plusMinutes(10)
        ));
        repository.save(session(
                foodCategoryId,
                "PRIVATE",
                DebateSessionStatus.COMPLETED,
                "비공개 완료",
                FIXED_TIME.plusMinutes(11)
        ));
        repository.save(session(
                foodCategoryId,
                "PUBLIC",
                DebateSessionStatus.RUNNING,
                "공개 진행 중",
                null
        ));
        repository.save(session(
                techCategoryId,
                "PUBLIC",
                DebateSessionStatus.COMPLETED,
                "기술 토론",
                FIXED_TIME.plusMinutes(12)
        ));

        entityManager.flush();
        entityManager.clear();

        var result = repository.findPublicCompleted("부먹", foodCategoryId, 0, 20);

        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.items())
                .extracting(DebateSession::getId)
                .containsExactly(visible.getId());
        assertThat(repository.findPublicCompletedById(visible.getId())).isPresent();
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

    private static DebateSession session() {
        return session(null, "PRIVATE", DebateSessionStatus.CREATED, "부먹 vs 찍먹", null);
    }

    private static DebateSession session(Long categoryId, String visibility,
                                         DebateSessionStatus status, String title,
                                         LocalDateTime endedAt) {
        return new DebateSession(
                null,
                1L,
                categoryId,
                title,
                "어느 방식이 더 나은가?",
                "food",
                visibility,
                status,
                DebateFormat.PROS_AND_CONS,
                5,
                status == DebateSessionStatus.COMPLETED ? 5 : 0,
                600,
                participants(),
                FIXED_TIME,
                status == DebateSessionStatus.CREATED ? null : FIXED_TIME.plusMinutes(1),
                endedAt
        );
    }

    private static List<DebateParticipant> participants() {
        return List.of(
                DebateParticipant.create(
                        10L,
                        0,
                        ParticipantModel.FAST,
                        "첫 번째 캐릭터",
                        "논리적인 캐릭터",
                        "{\"rationality\":90}",
                        "{\"tone\":\"차분함\"}"
                ),
                DebateParticipant.create(
                        20L,
                        1,
                        ParticipantModel.QUALITY,
                        "두 번째 캐릭터",
                        null,
                        null,
                        null
                )
        );
    }

}
