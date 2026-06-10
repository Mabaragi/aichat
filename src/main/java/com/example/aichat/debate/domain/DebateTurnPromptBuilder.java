package com.example.aichat.debate.domain;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DebateTurnPromptBuilder {

    public String buildDebateTurnPrompt(String topicTitle,
                                        String topicDescription,
                                        DebateFormat format,
                                        DebateParticipant participant,
                                        List<String> previousTurns,
                                        int maxTurnLength) {
        validateTopicTitle(topicTitle);
        validateTopicDescription(topicDescription);
        validateFormat(format);
        validateParticipant(participant);
        validateMaxTurnLength(maxTurnLength);

        return """
                당신은 AI 토론 플랫폼의 캐릭터입니다.

                [토론 주제]
                %s

                [주제 설명]
                %s

                [토론 형식]
                %s

                [당신의 참가자 모델]
                모델: %s

                [캐릭터]
                이름: %s
                설명: %s
                성격: %s
                말투: %s

                [이전 발화]
                %s

                [지시]
                위 정보를 바탕으로 참가자 모델의 응답 정책에 맞게 다음 발화를 작성하세요.
                상대의 이전 발화를 참고하되, 단순 반복하지 마세요.
                토론 주제에서 벗어나지 마세요.
                최대 %d자 이내로 작성하세요.
                """
                .formatted(
                        topicTitle.trim(),
                        topicDescription.trim(),
                        format.name(),
                        participant.getModel().name(),
                        participant.getName(),
                        display(participant.getDescription()),
                        display(participant.getPersonality()),
                        display(participant.getSpeechStyle()),
                        formatPreviousTurns(previousTurns),
                        maxTurnLength
                );
    }

    private static void validateTopicTitle(String topicTitle) {
        if (topicTitle == null || topicTitle.isBlank()) {
            throw new IllegalArgumentException("topicTitle is required");
        }
    }

    private static void validateTopicDescription(String topicDescription) {
        if (topicDescription == null || topicDescription.isBlank()) {
            throw new IllegalArgumentException("topicDescription is required");
        }
    }

    private static void validateFormat(DebateFormat format) {
        if (format == null) {
            throw new IllegalArgumentException("format is required");
        }
    }

    private static void validateParticipant(DebateParticipant participant) {
        if (participant == null) {
            throw new IllegalArgumentException("participant is required");
        }
    }

    private static void validateMaxTurnLength(int maxTurnLength) {
        if (maxTurnLength < 1) {
            throw new IllegalArgumentException("maxTurnLength must be at least 1");
        }
    }

    private static String formatPreviousTurns(List<String> previousTurns) {
        if (previousTurns == null || previousTurns.isEmpty()) {
            return "";
        }

        return previousTurns.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(turn -> !turn.isEmpty())
                .collect(Collectors.joining("\n"));
    }

    private static String display(String value) {
        return value == null || value.isBlank() ? "미설정" : value;
    }
}
