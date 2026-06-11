package com.example.aichat.debate.infrastructure;

import com.example.aichat.debate.domain.DebateSessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DebateSessionJpaRepository extends JpaRepository<DebateSessionJpaEntity, Long> {

    @Query("select coalesce(max(session.id), 0) from DebateSessionJpaEntity session")
    Long findMaxId();

    @Query(value = """
            select distinct session from DebateSessionJpaEntity session
            left join session.participants participant
            where session.visibility = 'PUBLIC'
              and session.status = :status
              and (:categoryId is null or session.categoryId = :categoryId)
              and (
                :query is null
                or lower(session.topicTitle) like lower(concat('%', :query, '%'))
                or lower(coalesce(session.topicDescription, '')) like lower(concat('%', :query, '%'))
                or lower(participant.name) like lower(concat('%', :query, '%'))
              )
            order by session.endedAt desc, session.id desc
            """,
            countQuery = """
            select count(distinct session) from DebateSessionJpaEntity session
            left join session.participants participant
            where session.visibility = 'PUBLIC'
              and session.status = :status
              and (:categoryId is null or session.categoryId = :categoryId)
              and (
                :query is null
                or lower(session.topicTitle) like lower(concat('%', :query, '%'))
                or lower(coalesce(session.topicDescription, '')) like lower(concat('%', :query, '%'))
                or lower(participant.name) like lower(concat('%', :query, '%'))
              )
            """)
    Page<DebateSessionJpaEntity> findPublicCompleted(
            DebateSessionStatus status,
            String query,
            Long categoryId,
            Pageable pageable
    );

    Optional<DebateSessionJpaEntity> findByIdAndVisibilityAndStatus(
            Long id,
            String visibility,
            DebateSessionStatus status
    );
}
