package com.example.aichat.cli.debate;

public record DebateNextTurnResult(
        int turnIndex,
        int participantCount,
        int maxRounds,
        int speakerIndex,
        int round,
        boolean shouldCompleteSession
) {
}
