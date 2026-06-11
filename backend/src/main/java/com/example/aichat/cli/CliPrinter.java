package com.example.aichat.cli;

import com.example.aichat.character.application.CharacterView;
import com.example.aichat.character.application.DeleteCharacterResult;
import com.example.aichat.character.application.ListCharactersResult;
import com.example.aichat.debate.application.DebateParticipantView;
import com.example.aichat.debate.application.DebateSessionView;
import com.example.aichat.cli.debate.DebateNextTurnResult;
import com.example.aichat.user.application.UserView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.PrintStream;

public class CliPrinter {

    private final ObjectMapper objectMapper;
    private final PrintStream out;
    private final PrintStream err;

    public CliPrinter(ObjectMapper objectMapper, PrintStream out, PrintStream err) {
        this.objectMapper = objectMapper;
        this.out = out;
        this.err = err;
    }

    public void printSuccess(OutputMode outputMode, Object payload) {
        if (outputMode == OutputMode.TEXT) {
            out.println(toText(payload));
            return;
        }

        out.println(toJson(payload));
    }

    public void printError(OutputMode outputMode, String code, String message) {
        var error = new CliErrorResponse(code, message);
        if (outputMode == OutputMode.TEXT) {
            err.println(code + ": " + message);
            return;
        }

        err.println(toJson(error));
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to render CLI output", exception);
        }
    }

    private String toText(Object payload) {
        if (payload instanceof CharacterView character) {
            return String.join(System.lineSeparator(),
                    "character",
                    "  id=" + character.id(),
                    "  ownerId=" + character.ownerId(),
                    "  name=" + character.name(),
                    "  description=" + valueOrNull(character.description()),
                    "  persona=" + valueOrNull(character.persona()),
                    "  visibility=" + character.visibility(),
                    "  createdAt=" + character.createdAt(),
                    "  updatedAt=" + character.updatedAt());
        }

        if (payload instanceof UserView user) {
            return String.join(System.lineSeparator(),
                    "user",
                    "  id=" + user.id(),
                    "  email=" + user.email(),
                    "  nickname=" + user.nickname(),
                    "  createdAt=" + user.createdAt());
        }

        if (payload instanceof DebateSessionView session) {
            String lineSeparator = System.lineSeparator();
            StringBuilder builder = new StringBuilder("debate-session")
                    .append(lineSeparator).append("  id=").append(session.id())
                    .append(lineSeparator).append("  ownerId=").append(session.ownerId())
                    .append(lineSeparator).append("  topicTitle=").append(session.topicTitle())
                    .append(lineSeparator).append("  topicDescription=")
                    .append(valueOrNull(session.topicDescription()))
                    .append(lineSeparator).append("  topicCategory=")
                    .append(valueOrNull(session.topicCategory()))
                    .append(lineSeparator).append("  status=").append(session.status())
                    .append(lineSeparator).append("  format=").append(session.format())
                    .append(lineSeparator).append("  maxRounds=").append(session.maxRounds())
                    .append(lineSeparator).append("  currentRound=").append(session.currentRound())
                    .append(lineSeparator).append("  maxTurnLength=").append(session.maxTurnLength())
                    .append(lineSeparator).append("  createdAt=").append(session.createdAt())
                    .append(lineSeparator).append("  participants=");

            for (DebateParticipantView participant : session.participants()) {
                builder.append(lineSeparator)
                        .append("    - position=").append(participant.position())
                        .append(", sourceCharacterId=").append(participant.sourceCharacterId())
                        .append(", model=").append(participant.model())
                        .append(", name=").append(participant.name())
                        .append(", description=").append(valueOrNull(participant.description()))
                        .append(", persona=").append(valueOrNull(participant.persona()));
            }
            return builder.toString();
        }

        if (payload instanceof DebateNextTurnResult nextTurn) {
            return String.join(System.lineSeparator(),
                    "debate-next-turn",
                    "  turnIndex=" + nextTurn.turnIndex(),
                    "  participantCount=" + nextTurn.participantCount(),
                    "  maxRounds=" + nextTurn.maxRounds(),
                    "  speakerIndex=" + nextTurn.speakerIndex(),
                    "  round=" + nextTurn.round(),
                    "  shouldCompleteSession=" + nextTurn.shouldCompleteSession());
        }

        if (payload instanceof ListCharactersResult list) {
            StringBuilder builder = new StringBuilder("characters");
            for (CharacterView character : list.items()) {
                builder.append(System.lineSeparator())
                        .append("- ")
                        .append(character.id())
                        .append(" ")
                        .append(character.name())
                        .append(" [")
                        .append(character.visibility())
                        .append("]");
            }
            return builder.toString();
        }

        if (payload instanceof DeleteCharacterResult deleted) {
            return "deleted characterId=" + deleted.characterId();
        }

        return String.valueOf(payload);
    }

    private String valueOrNull(String value) {
        return value == null ? "null" : value;
    }
}
