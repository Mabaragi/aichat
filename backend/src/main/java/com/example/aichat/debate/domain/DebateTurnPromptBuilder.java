package com.example.aichat.debate.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DebateTurnPromptBuilder {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

                [페르소나 규칙]
                %s

                [이전 발화]
                %s

                [지시]
                위 정보를 바탕으로 참가자 모델의 응답 정책과 페르소나 규칙에 맞게 다음 발화를 작성하세요.
                상대 주장을 먼저 요약하세요.
                핵심 전제, 근거, 논리 비약을 점검하세요.
                근거 또는 예시를 제시하세요.
                불확실한 사실은 단정하지 마세요.
                강한 근거가 나오면 입장을 일부 수정할 수 있습니다.
                페르소나보다 사실성, 안전, 토론 규칙을 우선하세요.
                상대의 이전 발화를 참고하되, 단순 반복하지 마세요.
                토론 주제에서 벗어나지 마세요.
                최대 %d자 이내로 작성하세요.

                [출력 형식]
                [요약]
                [핵심 반박]
                [근거 또는 예시]
                [질문]
                [입장 상태]
                """
                .formatted(
                        topicTitle.trim(),
                        topicDescription.trim(),
                        format.name(),
                        participant.getModel().name(),
                        participant.getName(),
                        display(participant.getDescription()),
                        formatPersona(participant.getPersona()),
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

    private static String formatPersona(String persona) {
        if (persona == null || persona.isBlank()) {
            return "미설정";
        }

        try {
            JsonNode root = OBJECT_MAPPER.readTree(persona);
            JsonNode voiceStyle = root.path("voiceStyle");
            JsonNode boundaries = root.path("boundaries");
            return """
                    정체성: %s
                    토론 역할: %s
                    핵심 가치: %s
                    전문 영역: %s
                    기본 관점: %s
                    근거 스타일: %s
                    토론 행동: %s
                    말투 톤: %s
                    문장 길이: %s
                    수사 방식: %s
                    자주 쓰는 표현: %s
                    반드시 할 것: %s
                    하지 말 것: %s
                    예시 발화: %s
                    """
                    .formatted(
                            text(root, "identity"),
                            text(root, "debateRole"),
                            array(root, "coreValues"),
                            array(root, "expertise"),
                            text(root, "defaultStance"),
                            text(root, "evidenceStyle"),
                            array(root, "debateBehavior"),
                            text(voiceStyle, "tone"),
                            text(voiceStyle, "sentenceLength"),
                            text(voiceStyle, "rhetoricalStyle"),
                            array(voiceStyle, "signaturePhrases"),
                            array(boundaries, "mustDo"),
                            array(boundaries, "mustNotDo"),
                            array(root, "exampleLines")
                    ).trim();
        } catch (Exception exception) {
            return persona;
        }
    }

    private static String text(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        if (value.isMissingNode() || value.isNull() || value.asText().isBlank()) {
            return "미설정";
        }
        return value.asText();
    }

    private static String array(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        if (!value.isArray() || value.isEmpty()) {
            return "미설정";
        }
        return java.util.stream.StreamSupport.stream(value.spliterator(), false)
                .filter(JsonNode::isTextual)
                .map(JsonNode::asText)
                .filter(item -> !item.isBlank())
                .collect(Collectors.joining(", "));
    }
}
