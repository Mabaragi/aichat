package com.example.aichat.cli.character;

import com.example.aichat.character.application.ListCharactersResult;
import com.example.aichat.character.application.ListCharactersUseCase;
import com.example.aichat.cli.CliPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Component
@Command(
        name = "list",
        mixinStandardHelpOptions = true,
        description = "List characters by owner ID."
)
public class CharacterListCommand implements Runnable {

    private final ListCharactersUseCase listCharactersUseCase;
    private final ObjectMapper objectMapper;

    @ParentCommand
    private CharacterCommand characterCommand;

    @Option(names = "--owner-id", required = true, description = "Owner user ID.")
    private Long ownerId;

    public CharacterListCommand(ListCharactersUseCase listCharactersUseCase,
                                ObjectMapper objectMapper) {
        this.listCharactersUseCase = listCharactersUseCase;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run() {
        ListCharactersResult result = listCharactersUseCase.execute(ownerId);
        new CliPrinter(objectMapper, System.out, System.err)
                .printSuccess(characterCommand.rootCommand().outputMode(), result);
    }
}
