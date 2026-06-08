package com.example.aichat.cli.character;

import com.example.aichat.character.application.UpdateCharacterCommand;
import com.example.aichat.character.application.UpdateCharacterUseCase;
import com.example.aichat.cli.CliPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Parameters;

@Component
@Command(
        name = "update",
        mixinStandardHelpOptions = true,
        description = "Update one character by character ID."
)
public class CharacterUpdateCommand implements Runnable {

    private final UpdateCharacterUseCase updateCharacterUseCase;
    private final ObjectMapper objectMapper;

    @ParentCommand
    private CharacterCommand characterCommand;

    @Parameters(index = "0", paramLabel = "CHARACTER_ID", description = "Character ID.")
    private Long characterId;

    @Option(names = "--name", description = "Character name.")
    private String name;

    @Option(names = "--description", description = "Character description.")
    private String description;

    @Option(names = "--personality", description = "Raw JSON personality payload.")
    private String personality;

    @Option(names = "--speech-style", description = "Raw JSON speech style payload.")
    private String speechStyle;

    @Option(names = "--visibility", description = "Visibility such as PRIVATE or PUBLIC.")
    private String visibility;

    public CharacterUpdateCommand(UpdateCharacterUseCase updateCharacterUseCase,
                                  ObjectMapper objectMapper) {
        this.updateCharacterUseCase = updateCharacterUseCase;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run() {
        UpdateCharacterCommand command = new UpdateCharacterCommand(
                characterId,
                name,
                description,
                CharacterCommandSupport.toPersonality(objectMapper, personality),
                CharacterCommandSupport.toSpeechStyle(objectMapper, speechStyle),
                visibility
        );

        new CliPrinter(objectMapper, System.out, System.err)
                .printSuccess(characterCommand.rootCommand().outputMode(), updateCharacterUseCase.execute(command));
    }
}
