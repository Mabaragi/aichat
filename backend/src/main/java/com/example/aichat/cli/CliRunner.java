package com.example.aichat.cli;

import com.example.aichat.cli.character.CharacterCommand;
import com.example.aichat.cli.character.CharacterCreateCommand;
import com.example.aichat.cli.character.CharacterDeleteCommand;
import com.example.aichat.cli.character.CharacterGetCommand;
import com.example.aichat.cli.character.CharacterListCommand;
import com.example.aichat.cli.character.CharacterUpdateCommand;
import com.example.aichat.cli.debate.DebateCommand;
import com.example.aichat.cli.debate.DebateCreateCommand;
import com.example.aichat.cli.debate.DebateNextTurnCommand;
import com.example.aichat.cli.user.UserCommand;
import com.example.aichat.cli.user.UserCreateCommand;
import com.example.aichat.cli.user.UserGetCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
public class CliRunner {

    private final RootCommand rootCommand;
    private final CharacterCommand characterCommand;
    private final CharacterCreateCommand characterCreateCommand;
    private final CharacterGetCommand characterGetCommand;
    private final CharacterListCommand characterListCommand;
    private final CharacterUpdateCommand characterUpdateCommand;
    private final CharacterDeleteCommand characterDeleteCommand;
    private final UserCommand userCommand;
    private final UserCreateCommand userCreateCommand;
    private final UserGetCommand userGetCommand;
    private final DebateCommand debateCommand;
    private final DebateCreateCommand debateCreateCommand;
    private final DebateNextTurnCommand debateNextTurnCommand;
    private final ObjectMapper objectMapper;

    public CliRunner(RootCommand rootCommand,
                     CharacterCommand characterCommand,
                     CharacterCreateCommand characterCreateCommand,
                     CharacterGetCommand characterGetCommand,
                     CharacterListCommand characterListCommand,
                     CharacterUpdateCommand characterUpdateCommand,
                     CharacterDeleteCommand characterDeleteCommand,
                     UserCommand userCommand,
                     UserCreateCommand userCreateCommand,
                     UserGetCommand userGetCommand,
                     DebateCommand debateCommand,
                     DebateCreateCommand debateCreateCommand,
                     DebateNextTurnCommand debateNextTurnCommand,
                     ObjectMapper objectMapper) {
        this.rootCommand = rootCommand;
        this.characterCommand = characterCommand;
        this.characterCreateCommand = characterCreateCommand;
        this.characterGetCommand = characterGetCommand;
        this.characterListCommand = characterListCommand;
        this.characterUpdateCommand = characterUpdateCommand;
        this.characterDeleteCommand = characterDeleteCommand;
        this.userCommand = userCommand;
        this.userCreateCommand = userCreateCommand;
        this.userGetCommand = userGetCommand;
        this.debateCommand = debateCommand;
        this.debateCreateCommand = debateCreateCommand;
        this.debateNextTurnCommand = debateNextTurnCommand;
        this.objectMapper = objectMapper;
    }

    public int run(String[] args) {
        CommandLine commandLine = new CommandLine(rootCommand);
        commandLine.setCaseInsensitiveEnumValuesAllowed(true);
        commandLine.addSubcommand("character", buildCharacterCommandLine());
        commandLine.addSubcommand("user", buildUserCommandLine());
        commandLine.addSubcommand("debate", buildDebateCommandLine());
        commandLine.setExecutionExceptionHandler((exception, cmd, parseResult) -> {
            OutputMode outputMode = rootCommand.outputMode();
            CliPrinter printer = new CliPrinter(objectMapper, System.out, System.err);
            if (exception instanceof com.example.aichat.common.exception.BusinessException businessException) {
                printer.printError(outputMode, businessException.getCode().name(), businessException.getMessage());
                return 2;
            }

            if (exception instanceof IllegalArgumentException illegalArgumentException) {
                printer.printError(outputMode, "INVALID_ARGUMENT", illegalArgumentException.getMessage());
                return 2;
            }

            printer.printError(outputMode, "UNEXPECTED_ERROR", exception.getMessage());
            return 1;
        });
        return commandLine.execute(args);
    }

    private CommandLine buildCharacterCommandLine() {
        CommandLine commandLine = new CommandLine(characterCommand);
        commandLine.addSubcommand("create", characterCreateCommand);
        commandLine.addSubcommand("get", characterGetCommand);
        commandLine.addSubcommand("list", characterListCommand);
        commandLine.addSubcommand("update", characterUpdateCommand);
        commandLine.addSubcommand("delete", characterDeleteCommand);
        return commandLine;
    }

    private CommandLine buildUserCommandLine() {
        CommandLine commandLine = new CommandLine(userCommand);
        commandLine.addSubcommand("create", userCreateCommand);
        commandLine.addSubcommand("get", userGetCommand);
        return commandLine;
    }

    private CommandLine buildDebateCommandLine() {
        CommandLine commandLine = new CommandLine(debateCommand);
        commandLine.addSubcommand("create", debateCreateCommand);
        commandLine.addSubcommand("next-turn", debateNextTurnCommand);
        return commandLine;
    }
}
