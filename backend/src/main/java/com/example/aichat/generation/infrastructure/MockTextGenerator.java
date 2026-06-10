package com.example.aichat.generation.infrastructure;

import com.example.aichat.generation.application.GenerationRequest;
import com.example.aichat.generation.application.GenerationResult;
import com.example.aichat.generation.application.TextGenerator;

public class MockTextGenerator implements TextGenerator {

    @Override
    public GenerationResult generate(GenerationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }

        return new GenerationResult(
                buildMockContent(request.prompt()),
                request.modelName(),
                0,
                0
        );
    }

    private static String buildMockContent(String prompt) {
        String topicTitle = extractSectionValue(prompt, "[토론 주제]");
        String participantModel = extractParticipantModel(prompt);

        if (topicTitle == null || participantModel == null) {
            return "mock response";
        }

        return participantModel + " 참가자가 '" + topicTitle + "'에 대해 다음 주장을 생성합니다.";
    }

    private static String extractSectionValue(String prompt, String header) {
        String[] lines = prompt.split("\\R");

        for (int index = 0; index < lines.length - 1; index++) {
            if (header.equals(lines[index].trim())) {
                return lines[index + 1].trim();
            }
        }

        return null;
    }

    private static String extractParticipantModel(String prompt) {
        String[] lines = prompt.split("\\R");

        for (int index = 0; index < lines.length - 1; index++) {
            if ("[당신의 참가자 모델]".equals(lines[index].trim())) {
                String modelLine = lines[index + 1].trim();
                String prefix = "모델: ";

                if (modelLine.startsWith(prefix)) {
                    return modelLine.substring(prefix.length()).trim();
                }

                return null;
            }
        }

        return null;
    }
}
