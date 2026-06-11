package com.example.aichat.debate.application;

import com.example.aichat.common.exception.BusinessException;
import com.example.aichat.common.exception.ErrorCode;
import com.example.aichat.debate.domain.DebateParticipant;
import com.example.aichat.debate.domain.DebateSession;
import com.example.aichat.debate.domain.DebateSessionRepository;
import com.example.aichat.debate.domain.DebateTurnRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ListPublicDebateTurnsUseCase {

    private final DebateSessionRepository debateSessionRepository;
    private final DebateTurnRepository debateTurnRepository;

    public ListPublicDebateTurnsUseCase(DebateSessionRepository debateSessionRepository,
                                        DebateTurnRepository debateTurnRepository) {
        this.debateSessionRepository = debateSessionRepository;
        this.debateTurnRepository = debateTurnRepository;
    }

    @Transactional(readOnly = true)
    public List<DebateTurnView> execute(Long sessionId) {
        DebateSession session = debateSessionRepository.findPublicCompletedById(sessionId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DEBATE_SESSION_NOT_FOUND,
                        "Debate session not found: " + sessionId
                ));
        Map<Long, DebateParticipant> participants = session.getParticipants().stream()
                .collect(Collectors.toMap(DebateParticipant::getId, Function.identity()));
        return debateTurnRepository.findBySessionIdOrderByTurnIndexAsc(session.getId()).stream()
                .map(turn -> DebateTurnView.from(
                        turn,
                        requireParticipant(participants, turn.getParticipantId()).getModel()
                ))
                .toList();
    }

    private static DebateParticipant requireParticipant(
            Map<Long, DebateParticipant> participants,
            Long participantId
    ) {
        DebateParticipant participant = participants.get(participantId);
        if (participant == null) {
            throw new BusinessException(
                    ErrorCode.DEBATE_PARTICIPANT_NOT_FOUND,
                    "Debate participant not found: " + participantId
            );
        }
        return participant;
    }
}
