package com.example.aichat.cli.character;

import com.example.aichat.character.application.DeleteCharacterUseCase;
import com.example.aichat.cli.CliPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Component
@Command(
        name = "delete",
        mixinStandardHelpOptions = true,
        description = "Delete one character by character ID."
)
public class CharacterDeleteCommand implements Runnable {

    private final DeleteCharacterUseCase deleteCharacterUseCase;
    private final ObjectMapper objectMapper;

    @ParentCommand
    private CharacterCommand characterCommand;

    @Parameters(index = "0", paramLabel = "CHARACTER_ID", description = "Character ID.")
    private Long characterId;

    public CharacterDeleteCommand(DeleteCharacterUseCase deleteCharacterUseCase,
                                  ObjectMapper objectMapper) {
        this.deleteCharacterUseCase = deleteCharacterUseCase;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run() {
        new CliPrinter(objectMapper, System.out, System.err)
                .printSuccess(characterCommand.rootCommand().outputMode(), deleteCharacterUseCase.execute(characterId));
    }
}
