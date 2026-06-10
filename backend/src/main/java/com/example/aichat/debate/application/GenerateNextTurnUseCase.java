package com.example.aichat.debate.application;

import org.springframework.stereotype.Component;

@Component
public class GenerateNextTurnUseCase {

    public int resolveSpeakerIndex(int turnIndex, int participantCount) {
        validateTurnIndex(turnIndex);
        validateParticipantCount(participantCount);
        return (turnIndex - 1) % participantCount;
    }

    public int resolveRound(int turnIndex, int participantCount) {
        validateTurnIndex(turnIndex);
        validateParticipantCount(participantCount);
        return ((turnIndex - 1) / participantCount) + 1;
    }

    public boolean shouldCompleteSession(int turnIndex, int maxRounds, int participantCount) {
        validateTurnIndex(turnIndex);
        validateMaxRounds(maxRounds);
        validateParticipantCount(participantCount);
        return turnIndex >= maxRounds * participantCount;
    }

    private static void validateTurnIndex(int turnIndex) {
        if (turnIndex < 1) {
            throw new IllegalArgumentException("turnIndex must be at least 1");
        }
    }

    private static void validateParticipantCount(int participantCount) {
        if (participantCount < 2) {
            throw new IllegalArgumentException("participantCount must be at least 2");
        }
    }

    private static void validateMaxRounds(int maxRounds) {
        if (maxRounds < 1) {
            throw new IllegalArgumentException("maxRounds must be at least 1");
        }
    }
}
