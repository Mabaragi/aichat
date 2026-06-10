package com.example.aichat.debate.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DebateTurnJpaRepository extends JpaRepository<DebateTurnJpaEntity, Integer> {

    List<DebateTurnJpaEntity> findBySession_IdOrderByTurnIndexAsc(Long sessionId);
}
