package com.example.aichat.debate.domain;

import lombok.Getter;

import com.example.aichat.common.domain.Visibility;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Getter
public class DebateSession {

    public static final int REQUIRED_PARTICIPANT_COUNT = 2;
    public static final int MIN_MAX_ROUNDS = 1;
    public static final int MAX_MAX_ROUNDS = 10;
    public static final int MIN_MAX_TURN_LENGTH = 100;
    public static final int MAX_MAX_TURN_LENGTH = 2000;
    public static final String DEFAULT_VISIBILITY = Visibility.DEFAULT;

    private final Long id;
    private final Long ownerId;
    private final Long categoryId;
    private final String topicTitle;
    private final String topicDescription;
    private final String topicCategory;
    private final String visibility;
    private DebateSessionStatus status;
    private final DebateFormat format;
    private final int maxRounds;
    private int currentRound;
    private final Integer maxTurnLength;
    private final List<DebateParticipant> participants;
    private final LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    public DebateSession(Long id, Long ownerId, String topicTitle, String topicDescription,
                         String topicCategory, DebateSessionStatus status, DebateFormat format,
                         int maxRounds, int currentRound, Integer maxTurnLength,
                         List<DebateParticipant> participants, LocalDateTime createdAt,
                         LocalDateTime startedAt, LocalDateTime endedAt) {
        this(id, ownerId, null, topicTitle, topicDescription, topicCategory,
                DEFAULT_VISIBILITY, status, format, maxRounds, currentRound,
                maxTurnLength, participants, createdAt, startedAt, endedAt);
    }

    public DebateSession(Long id, Long ownerId, Long categoryId, String topicTitle, String topicDescription,
                         String topicCategory, String visibility, DebateSessionStatus status, DebateFormat format,
                         int maxRounds, int currentRound, Integer maxTurnLength,
                         List<DebateParticipant> participants, LocalDateTime createdAt,
                         LocalDateTime startedAt, LocalDateTime endedAt) {
        validateOwnerId(ownerId);
        validateTopicTitle(topicTitle);
        validateStatus(status);
        validateFormat(format);
        validateMaxRounds(maxRounds);
        validateCurrentRound(currentRound, maxRounds);
        validateMaxTurnLength(maxTurnLength);
        validateParticipants(participants);
        validateTimestamp("createdAt", createdAt);

        this.id = id;
        this.ownerId = ownerId;
        this.categoryId = categoryId;
        this.topicTitle = topicTitle;
        this.topicDescription = topicDescription;
        this.topicCategory = topicCategory;
        this.visibility = Visibility.normalize(visibility);
        this.status = status;
        this.format = format;
        this.maxRounds = maxRounds;
        this.currentRound = currentRound;
        this.maxTurnLength = maxTurnLength;
        this.participants = participants.stream()
                .sorted(Comparator.comparingInt(DebateParticipant::getPosition))
                .toList();
        this.createdAt = createdAt;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    public static DebateSession create(Long ownerId, String topicTitle,
                                       String topicDescription, String topicCategory,
                                       DebateFormat format, int maxRounds,
                                       Integer maxTurnLength,
                                       List<DebateParticipant> participants,
                                       LocalDateTime createdAt) {
        return create(ownerId, null, topicTitle, topicDescription, topicCategory,
                DEFAULT_VISIBILITY, format, maxRounds, maxTurnLength, participants, createdAt);
    }

    public static DebateSession create(Long ownerId, Long categoryId, String topicTitle,
                                       String topicDescription, String topicCategory,
                                       String visibility, DebateFormat format, int maxRounds,
                                       Integer maxTurnLength,
                                       List<DebateParticipant> participants,
                                       LocalDateTime createdAt) {
        return new DebateSession(null, ownerId, categoryId, topicTitle, topicDescription, topicCategory,
                visibility, DebateSessionStatus.CREATED, format, maxRounds, 0, maxTurnLength,
                participants, createdAt, null, null);
    }

    public boolean canGenerateTurn() {
        return status == DebateSessionStatus.RUNNING;
    }

    public boolean isCompleted() {
        return status == DebateSessionStatus.COMPLETED;
    }

    public void start(LocalDateTime startedAt) {
        if (status != DebateSessionStatus.CREATED) {
            throw new IllegalStateException("session must be CREATED to start");
        }

        validateTimestamp("startedAt", startedAt);

        this.status = DebateSessionStatus.RUNNING;
        this.startedAt = startedAt;
    }

    public void registerTurn(int turnIndex, LocalDateTime generatedAt) {
        ensureRunning();
        validateTurnIndex(turnIndex);
        validateTimestamp("generatedAt", generatedAt);

        int participantCount = participants.size();
        int maxTurnCount = maxRounds * participantCount;

        if (turnIndex > maxTurnCount) {
            throw new IllegalArgumentException("turnIndex exceeds max rounds");
        }

        this.currentRound = ((turnIndex - 1) / participantCount) + 1;

        if (turnIndex == maxTurnCount) {
            complete(generatedAt);
        }
    }

    public void complete(LocalDateTime endedAt) {
        ensureRunning();
        validateTimestamp("endedAt", endedAt);

        this.status = DebateSessionStatus.COMPLETED;
        this.endedAt = endedAt;
    }

    private void ensureRunning() {
        if (status != DebateSessionStatus.RUNNING) {
            throw new IllegalStateException("session must be RUNNING to generate turns");
        }
    }

    private static void validateOwnerId(Long ownerId) {
        if (ownerId == null) {
            throw new IllegalArgumentException("ownerId is required");
        }
    }

    private static void validateTopicTitle(String topicTitle) {
        if (topicTitle == null || topicTitle.isBlank()) {
            throw new IllegalArgumentException("topicTitle is required");
        }
    }

    private static void validateStatus(DebateSessionStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("status is required");
        }
    }

    private static void validateFormat(DebateFormat format) {
        if (format == null) {
            throw new IllegalArgumentException("format is required");
        }
    }

    private static void validateMaxRounds(int maxRounds) {
        if (maxRounds < MIN_MAX_ROUNDS || maxRounds > MAX_MAX_ROUNDS) {
            throw new IllegalArgumentException("maxRounds out of range");
        }
    }

    private static void validateCurrentRound(int currentRound, int maxRounds) {
        if (currentRound < 0 || currentRound > maxRounds) {
            throw new IllegalArgumentException("currentRound out of range");
        }
    }

    private static void validateMaxTurnLength(Integer maxTurnLength) {
        if (maxTurnLength == null) {
            throw new IllegalArgumentException("maxTurnLength is required");
        }

        if (maxTurnLength < MIN_MAX_TURN_LENGTH || maxTurnLength > MAX_MAX_TURN_LENGTH) {
            throw new IllegalArgumentException("maxTurnLength out of range");
        }
    }

    private static void validateParticipants(List<DebateParticipant> participants) {
        if (participants == null) {
            throw new IllegalArgumentException("participants are required");
        }

        if (participants.size() != REQUIRED_PARTICIPANT_COUNT) {
            throw new IllegalArgumentException("participants must be exactly 2");
        }

        List<Integer> positions = participants.stream()
                .map(DebateParticipant::getPosition)
                .sorted()
                .toList();

        if (!positions.equals(List.of(0, 1))) {
            throw new IllegalArgumentException("participant positions must be 0 and 1");
        }
    }

    private static void validateTurnIndex(int turnIndex) {
        if (turnIndex < 1) {
            throw new IllegalArgumentException("turnIndex must be at least 1");
        }
    }

    private static void validateTimestamp(String fieldName, LocalDateTime timestamp) {
        if (timestamp == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}
