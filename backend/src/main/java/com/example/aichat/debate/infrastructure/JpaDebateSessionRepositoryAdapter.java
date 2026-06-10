package com.example.aichat.debate.infrastructure;

import com.example.aichat.debate.domain.DebateParticipant;
import com.example.aichat.debate.domain.DebateSession;
import com.example.aichat.debate.domain.DebateSessionRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class JpaDebateSessionRepositoryAdapter implements DebateSessionRepository {

    private final DebateSessionJpaRepository jpaRepository;
    private final AtomicLong nextSessionId = new AtomicLong(-1);

    public JpaDebateSessionRepositoryAdapter(DebateSessionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public DebateSession save(DebateSession session) {
        Long sessionId = session.getId() == null
                ? nextSessionId()
                : session.getId();
        return toDomain(jpaRepository.saveAndFlush(toEntity(session, sessionId)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DebateSession> findById(Long sessionId) {
        return jpaRepository.findById(sessionId)
                .map(JpaDebateSessionRepositoryAdapter::toDomain);
    }

    private static DebateSessionJpaEntity toEntity(DebateSession session, Long sessionId) {
        DebateSessionJpaEntity entity = new DebateSessionJpaEntity(
                sessionId,
                session.getOwnerId(),
                session.getTopicTitle(),
                session.getTopicDescription(),
                session.getTopicCategory(),
                session.getStatus(),
                session.getFormat(),
                session.getMaxRounds(),
                session.getCurrentRound(),
                session.getMaxTurnLength(),
                session.getCreatedAt(),
                session.getStartedAt(),
                session.getEndedAt()
        );

        session.getParticipants().stream()
                .map(JpaDebateSessionRepositoryAdapter::toEntity)
                .forEach(entity::addParticipant);

        return entity;
    }

    private static DebateParticipantJpaEntity toEntity(DebateParticipant participant) {
        return new DebateParticipantJpaEntity(
                toInteger(participant.getId()),
                participant.getSourceCharacterId(),
                participant.getPosition(),
                participant.getModel(),
                participant.getName(),
                participant.getDescription(),
                participant.getPersonality(),
                participant.getSpeechStyle()
        );
    }

    private static DebateSession toDomain(DebateSessionJpaEntity entity) {
        return new DebateSession(
                entity.id(),
                entity.ownerId(),
                entity.topicTitle(),
                entity.topicDescription(),
                entity.topicCategory(),
                entity.status(),
                entity.format(),
                entity.maxRounds(),
                entity.currentRound(),
                entity.maxTurnLength(),
                entity.participants().stream()
                        .map(JpaDebateSessionRepositoryAdapter::toDomain)
                        .toList(),
                entity.createdAt(),
                entity.startedAt(),
                entity.endedAt()
        );
    }

    private static DebateParticipant toDomain(DebateParticipantJpaEntity entity) {
        return new DebateParticipant(
                entity.id().longValue(),
                entity.sourceCharacterId(),
                entity.position(),
                entity.model(),
                entity.name(),
                entity.description(),
                entity.personality(),
                entity.speechStyle()
        );
    }

    private static Integer toInteger(Long id) {
        return id == null ? null : Math.toIntExact(id);
    }

    private synchronized long nextSessionId() {
        if (nextSessionId.get() < 0) {
            nextSessionId.set(jpaRepository.findMaxId());
        }
        return nextSessionId.incrementAndGet();
    }
}
