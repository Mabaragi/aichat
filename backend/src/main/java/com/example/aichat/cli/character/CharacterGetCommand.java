package com.example.aichat.cli.character;

import com.example.aichat.character.application.CharacterView;
import com.example.aichat.character.application.GetCharacterUseCase;
import com.example.aichat.cli.CliPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Component
@Command(
        name = "get",
        mixinStandardHelpOptions = true,
        description = "Get one character by character ID."
)
public class CharacterGetCommand implements Runnable {

    private final GetCharacterUseCase getCharacterUseCase;
    private final ObjectMapper objectMapper;

    @ParentCommand
    private CharacterCommand characterCommand;

    @Parameters(index = "0", paramLabel = "CHARACTER_ID", description = "Character ID.")
    private Long characterId;

    public CharacterGetCommand(GetCharacterUseCase getCharacterUseCase,
                               ObjectMapper objectMapper) {
        this.getCharacterUseCase = getCharacterUseCase;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run() {
        CharacterView result = getCharacterUseCase.execute(characterId);
        new CliPrinter(objectMapper, System.out, System.err)
                .printSuccess(characterCommand.rootCommand().outputMode(), result);
    }
}
