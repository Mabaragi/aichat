package com.example.aichat.debate.domain;

import java.util.List;

public interface DebateTurnRepository {

    DebateTurn save(DebateTurn turn);

    List<DebateTurn> findBySessionIdOrderByTurnIndexAsc(Long sessionId);
}
