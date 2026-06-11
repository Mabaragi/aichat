package com.example.aichat.debate.infrastructure;

import com.example.aichat.debate.domain.DebateFormat;
import com.example.aichat.debate.domain.DebateParticipant;
import com.example.aichat.debate.domain.DebateSession;
import com.example.aichat.debate.domain.DebateSessionRepository;
import com.example.aichat.debate.domain.DebateTurn;
import com.example.aichat.debate.domain.DebateTurnRepository;
import com.example.aichat.debate.domain.ParticipantModel;
import com.example.aichat.debate.domain.TurnStatus;
import com.example.aichat.debate.domain.TurnType;
import com.example.aichat.support.PersonaFixtures;
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
@Import({
        JpaDebateSessionRepositoryAdapter.class,
        JpaDebateTurnRepositoryAdapter.class
})
class DebateTurnJpaRepositoryTest {

    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2026, 6, 10, 12, 0);
    private static final LocalDateTime TURN_ONE_TIME = LocalDateTime.of(2026, 6, 10, 12, 2);
    private static final LocalDateTime TURN_TWO_TIME = LocalDateTime.of(2026, 6, 10, 12, 3);

    @Autowired
    private DebateSessionRepository debateSessionRepository;

    @Autowired
    private DebateTurnRepository debateTurnRepository;

    @Autowired
    private DebateSessionJpaRepository debateSessionJpaRepository;

    @Autowired
    private DebateTurnJpaRepository debateTurnJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void cleanDatabase() {
        debateTurnJpaRepository.deleteAll();
        debateTurnJpaRepository.flush();
        debateSessionJpaRepository.deleteAll();
        debateSessionJpaRepository.flush();
        entityManager.clear();
    }

    @Test
    void saveAndFindBySessionIdRoundTripsTurnsInTurnIndexOrder() {
        DebateSession session = debateSessionRepository.save(session());

        Long firstParticipantId = session.getParticipants().get(0).getId();
        Long secondParticipantId = session.getParticipants().get(1).getId();

        DebateTurn secondTurn = debateTurnRepository.save(DebateTurn.create(
                session.getId(),
                secondParticipantId,
                1,
                2,
                TurnType.REBUTTAL,
                TurnStatus.COMPLETED,
                "두 번째 발화",
                "prompt-2",
                "gpt-5-mini-2025-08-07",
                8,
                4,
                TURN_TWO_TIME
        ));
        DebateTurn firstTurn = debateTurnRepository.save(DebateTurn.create(
                session.getId(),
                firstParticipantId,
                1,
                1,
                TurnType.ARGUMENT,
                TurnStatus.COMPLETED,
                "첫 번째 발화",
                "prompt-1",
                "gpt-5-mini-2025-08-07",
                5,
                3,
                TURN_ONE_TIME
        ));

        entityManager.flush();
        entityManager.clear();

        assertThat(firstTurn.getId()).isNotNull();
        assertThat(secondTurn.getId()).isNotNull();

        List<DebateTurn> turns = debateTurnRepository.findBySessionIdOrderByTurnIndexAsc(session.getId());

        assertThat(turns)
                .extracting(DebateTurn::getTurnIndex)
                .containsExactly(1, 2);
        assertThat(turns)
                .extracting(DebateTurn::getContent)
                .containsExactly("첫 번째 발화", "두 번째 발화");
        assertThat(turns)
                .extracting(DebateTurn::getModelName)
                .containsExactly("gpt-5-mini-2025-08-07", "gpt-5-mini-2025-08-07");
        assertThat(turns)
                .extracting(DebateTurn::getSessionId)
                .containsOnly(session.getId());
    }

    private static DebateSession session() {
        return DebateSession.create(
                1L,
                "부먹 vs 찍먹",
                "어느 방식이 더 나은가?",
                "FOOD",
                DebateFormat.PROS_AND_CONS,
                1,
                600,
                List.of(
                        DebateParticipant.create(
                                10L,
                                0,
                                ParticipantModel.MOCK,
                                "첫 번째 캐릭터",
                                "논리적인 캐릭터",
                                PersonaFixtures.rationalGourmetJson()
                        ),
                        DebateParticipant.create(
                                20L,
                                1,
                                ParticipantModel.FAST,
                                "두 번째 캐릭터",
                                "도전적인 캐릭터",
                                PersonaFixtures.empatheticJson()
                        )
                ),
                FIXED_TIME
        );
    }
}
