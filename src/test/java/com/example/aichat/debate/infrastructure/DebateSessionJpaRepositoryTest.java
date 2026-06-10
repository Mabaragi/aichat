package com.example.aichat.debate.infrastructure;

import com.example.aichat.debate.domain.DebateFormat;
import com.example.aichat.debate.domain.DebateParticipant;
import com.example.aichat.debate.domain.DebateSession;
import com.example.aichat.debate.domain.DebateSessionRepository;
import com.example.aichat.debate.domain.ParticipantModel;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

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

    private static DebateSession session() {
        return DebateSession.create(
                1L,
                "부먹 vs 찍먹",
                "어느 방식이 더 나은가?",
                "FOOD",
                DebateFormat.PROS_AND_CONS,
                5,
                600,
                List.of(
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
                ),
                FIXED_TIME
        );
    }
}
