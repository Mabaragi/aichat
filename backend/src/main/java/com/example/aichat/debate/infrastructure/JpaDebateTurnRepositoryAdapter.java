package com.example.aichat.debate.infrastructure;

import com.example.aichat.debate.domain.DebateTurn;
import com.example.aichat.debate.domain.DebateTurnRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class JpaDebateTurnRepositoryAdapter implements DebateTurnRepository {

    private final DebateTurnJpaRepository jpaRepository;
    private final DebateSessionJpaRepository debateSessionJpaRepository;

    public JpaDebateTurnRepositoryAdapter(DebateTurnJpaRepository jpaRepository,
                                          DebateSessionJpaRepository debateSessionJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.debateSessionJpaRepository = debateSessionJpaRepository;
    }

    @Override
    @Transactional
    public DebateTurn save(DebateTurn turn) {
        DebateTurnJpaEntity entity = toEntity(turn);
        entity.attachTo(debateSessionJpaRepository.getReferenceById(turn.getSessionId()));
        return toDomain(jpaRepository.saveAndFlush(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DebateTurn> findBySessionIdOrderByTurnIndexAsc(Long sessionId) {
        return jpaRepository.findBySession_IdOrderByTurnIndexAsc(sessionId).stream()
                .map(JpaDebateTurnRepositoryAdapter::toDomain)
                .toList();
    }

    private static DebateTurnJpaEntity toEntity(DebateTurn turn) {
        return new DebateTurnJpaEntity(
                toInteger(turn.getId()),
                turn.getParticipantId(),
                turn.getRound(),
                turn.getTurnIndex(),
                turn.getType(),
                turn.getStatus(),
                turn.getContent(),
                turn.getPromptSnapshot(),
                turn.getModelName(),
                turn.getInputTokens(),
                turn.getOutputTokens(),
                turn.getCreatedAt()
        );
    }

    private static DebateTurn toDomain(DebateTurnJpaEntity entity) {
        return new DebateTurn(
                entity.id() == null ? null : entity.id().longValue(),
                entity.sessionId(),
                entity.participantId(),
                entity.round(),
                entity.turnIndex(),
                entity.type(),
                entity.status(),
                entity.content(),
                entity.promptSnapshot(),
                entity.modelName(),
                entity.inputTokens(),
                entity.outputTokens(),
                entity.createdAt()
        );
    }

    private static Integer toInteger(Long id) {
        return id == null ? null : Math.toIntExact(id);
    }
}
