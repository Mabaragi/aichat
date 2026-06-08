package com.example.aichat.cli;

import com.example.aichat.character.application.CharacterView;
import com.example.aichat.character.application.DeleteCharacterResult;
import com.example.aichat.character.application.ListCharactersResult;
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
                    "  personality=" + valueOrNull(character.personality()),
                    "  speechStyle=" + valueOrNull(character.speechStyle()),
                    "  visibility=" + character.visibility(),
                    "  createdAt=" + character.createdAt(),
                    "  updatedAt=" + character.updatedAt());
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
