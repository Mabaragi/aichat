package com.example.aichat.cli.character;

import com.example.aichat.character.application.CreateCharacterCommand;
import com.example.aichat.character.application.CreateCharacterUseCase;
import com.example.aichat.cli.CliPrinter;
import com.example.aichat.cli.OutputMode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Component
@Command(
        name = "create",
        mixinStandardHelpOptions = true,
        description = "Create a character."
)
public class CharacterCreateCommand implements Runnable {

    private final CreateCharacterUseCase createCharacterUseCase;
    private final ObjectMapper objectMapper;

    @ParentCommand
    private CharacterCommand characterCommand;

    @Option(names = "--owner-id", required = true, description = "Owner user ID.")
    private Long ownerId;

    @Option(names = "--name", required = true, description = "Character name.")
    private String name;

    @Option(names = "--description", description = "Character description.")
    private String description;

    @Option(names = "--personality", description = "Raw JSON personality payload.")
    private String personality;

    @Option(names = "--speech-style", description = "Raw JSON speech style payload.")
    private String speechStyle;

    @Option(names = "--visibility", description = "Visibility such as PRIVATE or PUBLIC.")
    private String visibility;

    public CharacterCreateCommand(CreateCharacterUseCase createCharacterUseCase,
                                  ObjectMapper objectMapper) {
        this.createCharacterUseCase = createCharacterUseCase;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run() {
        CreateCharacterCommand command = new CreateCharacterCommand(
                ownerId,
                name,
                description,
                CharacterCommandSupport.normalizeJson(objectMapper, personality, "--personality"),
                CharacterCommandSupport.normalizeJson(objectMapper, speechStyle, "--speech-style"),
                visibility
        );

        var result = createCharacterUseCase.execute(command);
        new CliPrinter(objectMapper, System.out, System.err)
                .printSuccess(characterCommand.rootCommand().outputMode(), result);
    }
}
