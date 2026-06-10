package com.example.aichat.cli.debate;

import com.example.aichat.debate.application.CreateDebateParticipantCommand;
import com.example.aichat.debate.domain.DebateSession;
import com.example.aichat.debate.domain.ParticipantModel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Locale;

final class DebateCommandSupport {

    private DebateCommandSupport() {
    }

    static List<CreateDebateParticipantCommand> toParticipantCommands(ObjectMapper objectMapper,
                                                                      List<String> rawParticipants) {
        if (rawParticipants == null || rawParticipants.size() != DebateSession.REQUIRED_PARTICIPANT_COUNT) {
            throw new IllegalArgumentException("participants must be exactly 2");
        }

        return rawParticipants.stream()
                .map(rawParticipant -> toParticipantCommand(objectMapper, rawParticipant))
                .toList();
    }

    private static CreateDebateParticipantCommand toParticipantCommand(ObjectMapper objectMapper,
                                                                        String rawParticipant) {
        JsonNode participant = parseJson(objectMapper, rawParticipant, "--participant");
        if (!participant.isObject()) {
            throw new IllegalArgumentException("--participant must be a JSON object");
        }

        JsonNode characterIdNode = participant.get("characterId");
        if (characterIdNode == null || !characterIdNode.isIntegralNumber()) {
            throw new IllegalArgumentException("--participant.characterId must be a number");
        }

        JsonNode modelNode = participant.get("model");
        if (modelNode == null || !modelNode.isTextual() || modelNode.asText().isBlank()) {
            throw new IllegalArgumentException("--participant.model must be a string");
        }

        ParticipantModel model;
        try {
            model = ParticipantModel.valueOf(modelNode.asText().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "--participant.model must be one of MOCK, FAST, BALANCED, QUALITY");
        }

        return new CreateDebateParticipantCommand(characterIdNode.asLong(), model);
    }

    private static JsonNode parseJson(ObjectMapper objectMapper, String rawJson, String optionName) {
        try {
            return objectMapper.readTree(rawJson);
        } catch (Exception exception) {
            throw new IllegalArgumentException(optionName + " must be valid JSON");
        }
    }
}
