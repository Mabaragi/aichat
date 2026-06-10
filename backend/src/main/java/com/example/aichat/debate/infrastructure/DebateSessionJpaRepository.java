package com.example.aichat.debate.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DebateSessionJpaRepository extends JpaRepository<DebateSessionJpaEntity, Long> {

    @Query("select coalesce(max(session.id), 0) from DebateSessionJpaEntity session")
    Long findMaxId();
}
