package com.example.aichat.cli;

import com.example.aichat.cli.character.CharacterCommand;
import com.example.aichat.cli.character.CharacterCreateCommand;
import com.example.aichat.cli.character.CharacterDeleteCommand;
import com.example.aichat.cli.character.CharacterGetCommand;
import com.example.aichat.cli.character.CharacterListCommand;
import com.example.aichat.cli.character.CharacterUpdateCommand;
import com.example.aichat.character.application.CharacterView;
import com.example.aichat.character.application.CreateCharacterUseCase;
import com.example.aichat.character.application.DeleteCharacterResult;
import com.example.aichat.character.application.DeleteCharacterUseCase;
import com.example.aichat.character.application.GetCharacterUseCase;
import com.example.aichat.character.application.ListCharactersResult;
import com.example.aichat.character.application.ListCharactersUseCase;
import com.example.aichat.character.application.UpdateCharacterUseCase;
import com.example.aichat.common.exception.BusinessException;
import com.example.aichat.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CliCommandTest {

    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void helpShowsCharacterCommands() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        installStreams(out, err);

        CommandLine root = commandLine(
                new RootCommand(),
                new CharacterCommand(),
                new CharacterCreateCommand(mock(CreateCharacterUseCase.class), new ObjectMapper()),
                new CharacterGetCommand(mock(GetCharacterUseCase.class), new ObjectMapper()),
                new CharacterListCommand(mock(ListCharactersUseCase.class), new ObjectMapper()),
                new CharacterUpdateCommand(mock(UpdateCharacterUseCase.class), new ObjectMapper()),
                new CharacterDeleteCommand(mock(DeleteCharacterUseCase.class), new ObjectMapper())
        );

        int exitCode = root.execute("character", "--help");

        assertThat(exitCode).isZero();
        assertThat(text(out)).contains("Character management commands");
        assertThat(text(err)).isEmpty();
    }

    @Test
    void missingRequiredOptionFailsParsing() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        installStreams(out, err);

        CommandLine root = commandLine(
                new RootCommand(),
                new CharacterCommand(),
                new CharacterCreateCommand(mock(CreateCharacterUseCase.class), new ObjectMapper()),
                new CharacterGetCommand(mock(GetCharacterUseCase.class), new ObjectMapper()),
                new CharacterListCommand(mock(ListCharactersUseCase.class), new ObjectMapper()),
                new CharacterUpdateCommand(mock(UpdateCharacterUseCase.class), new ObjectMapper()),
                new CharacterDeleteCommand(mock(DeleteCharacterUseCase.class), new ObjectMapper())
        );

        int exitCode = root.execute("character", "create", "--owner-id", "1");

        assertThat(exitCode).isNotZero();
        assertThat(text(err)).contains("Missing required option");
    }

    @Test
    void invalidJsonFailsBeforeUseCaseInvocation() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        installStreams(out, err);

        CreateCharacterUseCase createCharacterUseCase = mock(CreateCharacterUseCase.class);

        CommandLine root = commandLine(
                new RootCommand(),
                new CharacterCommand(),
                new CharacterCreateCommand(createCharacterUseCase, new ObjectMapper()),
                new CharacterGetCommand(mock(GetCharacterUseCase.class), new ObjectMapper()),
                new CharacterListCommand(mock(ListCharactersUseCase.class), new ObjectMapper()),
                new CharacterUpdateCommand(mock(UpdateCharacterUseCase.class), new ObjectMapper()),
                new CharacterDeleteCommand(mock(DeleteCharacterUseCase.class), new ObjectMapper())
        );

        int exitCode = root.execute("--output", "TEXT", "character", "create",
                "--owner-id", "1",
                "--name", "합리주의 미식가",
                "--personality", "{invalid-json}");

        assertThat(exitCode).isEqualTo(2);
        assertThat(text(err)).contains("INVALID_ARGUMENT");
        verifyNoInteractions(createCharacterUseCase);
    }

    @Test
    void invalidCharacterIdFailsParsing() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        installStreams(out, err);

        CommandLine root = commandLine(
                new RootCommand(),
                new CharacterCommand(),
                new CharacterCreateCommand(mock(CreateCharacterUseCase.class), new ObjectMapper()),
                new CharacterGetCommand(mock(GetCharacterUseCase.class), new ObjectMapper()),
                new CharacterListCommand(mock(ListCharactersUseCase.class), new ObjectMapper()),
                new CharacterUpdateCommand(mock(UpdateCharacterUseCase.class), new ObjectMapper()),
                new CharacterDeleteCommand(mock(DeleteCharacterUseCase.class), new ObjectMapper())
        );

        int exitCode = root.execute("character", "get", "abc");

        assertThat(exitCode).isNotZero();
        assertThat(text(err)).contains("Invalid value for positional parameter");
    }

    @Test
    void textOutputUsesTextFormatter() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        installStreams(out, err);

        CreateCharacterUseCase createCharacterUseCase = mock(CreateCharacterUseCase.class);
        when(createCharacterUseCase.execute(any())).thenReturn(characterView());

        CommandLine root = commandLine(
                new RootCommand(),
                new CharacterCommand(),
                new CharacterCreateCommand(createCharacterUseCase, new ObjectMapper()),
                new CharacterGetCommand(mock(GetCharacterUseCase.class), new ObjectMapper()),
                new CharacterListCommand(mock(ListCharactersUseCase.class), new ObjectMapper()),
                new CharacterUpdateCommand(mock(UpdateCharacterUseCase.class), new ObjectMapper()),
                new CharacterDeleteCommand(mock(DeleteCharacterUseCase.class), new ObjectMapper())
        );

        int exitCode = root.execute("--output", "TEXT", "character", "create",
                "--owner-id", "1",
                "--name", "합리주의 미식가",
                "--description", "논리적",
                "--personality", "{\"rationality\":90}",
                "--speech-style", "{\"tone\":\"차분함\"}");

        assertThat(exitCode).isZero();
        assertThat(text(out)).contains("character", "name=합리주의 미식가");
        assertThat(text(err)).isEmpty();
    }

    @Test
    void businessExceptionProducesExitCodeTwo() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        installStreams(out, err);

        GetCharacterUseCase getCharacterUseCase = mock(GetCharacterUseCase.class);
        when(getCharacterUseCase.execute(99L)).thenThrow(new BusinessException(
                ErrorCode.CHARACTER_NOT_FOUND,
                "Character not found: 99"
        ));

        CommandLine root = commandLine(
                new RootCommand(),
                new CharacterCommand(),
                new CharacterCreateCommand(mock(CreateCharacterUseCase.class), new ObjectMapper()),
                new CharacterGetCommand(getCharacterUseCase, new ObjectMapper()),
                new CharacterListCommand(mock(ListCharactersUseCase.class), new ObjectMapper()),
                new CharacterUpdateCommand(mock(UpdateCharacterUseCase.class), new ObjectMapper()),
                new CharacterDeleteCommand(mock(DeleteCharacterUseCase.class), new ObjectMapper())
        );

        int exitCode = root.execute("character", "get", "99");

        assertThat(exitCode).isEqualTo(2);
        assertThat(text(err)).contains("CHARACTER_NOT_FOUND");
    }

    private static CommandLine commandLine(RootCommand rootCommand,
                                           CharacterCommand characterCommand,
                                           CharacterCreateCommand createCommand,
                                           CharacterGetCommand getCommand,
                                           CharacterListCommand listCommand,
                                           CharacterUpdateCommand updateCommand,
                                           CharacterDeleteCommand deleteCommand) {
        CommandLine root = new CommandLine(rootCommand);
        CommandLine character = new CommandLine(characterCommand);
        character.addSubcommand("create", createCommand);
        character.addSubcommand("get", getCommand);
        character.addSubcommand("list", listCommand);
        character.addSubcommand("update", updateCommand);
        character.addSubcommand("delete", deleteCommand);
        root.addSubcommand("character", character);
        root.setExecutionExceptionHandler((exception, commandLine, parseResult) -> {
            CliPrinter printer = new CliPrinter(new ObjectMapper(), System.out, System.err);
            if (exception instanceof BusinessException businessException) {
                printer.printError(rootCommand.outputMode(), businessException.getCode().name(), businessException.getMessage());
                return 2;
            }

            if (exception instanceof IllegalArgumentException illegalArgumentException) {
                printer.printError(rootCommand.outputMode(), "INVALID_ARGUMENT", illegalArgumentException.getMessage());
                return 2;
            }

            printer.printError(rootCommand.outputMode(), "UNEXPECTED_ERROR", exception.getMessage());
            return 1;
        });
        return root;
    }

    private static CharacterView characterView() {
        return new CharacterView(
                1L,
                1L,
                "합리주의 미식가",
                "논리적",
                "{\"rationality\":90}",
                "{\"tone\":\"차분함\"}",
                "PRIVATE",
                LocalDateTime.of(2026, 6, 8, 12, 0),
                LocalDateTime.of(2026, 6, 8, 12, 0)
        );
    }

    private static void installStreams(ByteArrayOutputStream out, ByteArrayOutputStream err) {
        System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(err, true, StandardCharsets.UTF_8));
    }

    private static String text(ByteArrayOutputStream stream) {
        return stream.toString(StandardCharsets.UTF_8);
    }
}
