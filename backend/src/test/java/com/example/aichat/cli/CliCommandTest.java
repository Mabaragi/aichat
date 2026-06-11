package com.example.aichat.cli;

import com.example.aichat.character.application.CharacterView;
import com.example.aichat.character.application.CreateCharacterUseCase;
import com.example.aichat.character.application.DeleteCharacterUseCase;
import com.example.aichat.character.application.GetCharacterUseCase;
import com.example.aichat.character.application.ListCharactersUseCase;
import com.example.aichat.character.application.UpdateCharacterUseCase;
import com.example.aichat.cli.character.CharacterCommand;
import com.example.aichat.cli.character.CharacterCreateCommand;
import com.example.aichat.cli.character.CharacterDeleteCommand;
import com.example.aichat.cli.character.CharacterGetCommand;
import com.example.aichat.cli.character.CharacterListCommand;
import com.example.aichat.cli.character.CharacterUpdateCommand;
import com.example.aichat.cli.debate.DebateCommand;
import com.example.aichat.cli.debate.DebateCreateCommand;
import com.example.aichat.cli.debate.DebateNextTurnCommand;
import com.example.aichat.cli.debate.DebateNextTurnResult;
import com.example.aichat.cli.user.UserCommand;
import com.example.aichat.cli.user.UserCreateCommand;
import com.example.aichat.cli.user.UserGetCommand;
import com.example.aichat.common.exception.BusinessException;
import com.example.aichat.common.exception.ErrorCode;
import com.example.aichat.debate.application.CreateDebateSessionUseCase;
import com.example.aichat.debate.application.DebateParticipantView;
import com.example.aichat.debate.application.DebateSessionView;
import com.example.aichat.debate.application.GenerateNextTurnUseCase;
import com.example.aichat.debate.domain.DebateFormat;
import com.example.aichat.debate.domain.DebateSessionStatus;
import com.example.aichat.debate.domain.ParticipantModel;
import com.example.aichat.support.PersonaFixtures;
import com.example.aichat.user.application.CreateUserUseCase;
import com.example.aichat.user.application.GetUserUseCase;
import com.example.aichat.user.application.UserView;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

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
    void helpShowsCharacterUserAndDebateCommands() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        installStreams(out, err);

        CommandLine root = commandLine(
                new RootCommand(),
                mock(CreateCharacterUseCase.class),
                mock(GetCharacterUseCase.class),
                mock(ListCharactersUseCase.class),
                mock(UpdateCharacterUseCase.class),
                mock(DeleteCharacterUseCase.class),
                mock(CreateUserUseCase.class),
                mock(GetUserUseCase.class),
                mock(CreateDebateSessionUseCase.class),
                new GenerateNextTurnUseCase()
        );

        int exitCode = root.execute("--help");

        assertThat(exitCode).isZero();
        assertThat(text(out)).contains("Character management commands");
        assertThat(text(out)).contains("User commands");
        assertThat(text(out)).contains("Debate commands");
    }

    @Test
    void missingRequiredOptionFailsParsingForUserCreate() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        installStreams(out, err);

        CommandLine root = commandLine(
                new RootCommand(),
                mock(CreateCharacterUseCase.class),
                mock(GetCharacterUseCase.class),
                mock(ListCharactersUseCase.class),
                mock(UpdateCharacterUseCase.class),
                mock(DeleteCharacterUseCase.class),
                mock(CreateUserUseCase.class),
                mock(GetUserUseCase.class),
                mock(CreateDebateSessionUseCase.class),
                new GenerateNextTurnUseCase()
        );

        int exitCode = root.execute("user", "create", "--email", "user@example.com");

        assertThat(exitCode).isNotZero();
        assertThat(text(err)).contains("Missing required option");
    }

    @Test
    void missingRequiredOptionFailsParsingForDebateCreate() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        installStreams(out, err);

        CommandLine root = commandLine(
                new RootCommand(),
                mock(CreateCharacterUseCase.class),
                mock(GetCharacterUseCase.class),
                mock(ListCharactersUseCase.class),
                mock(UpdateCharacterUseCase.class),
                mock(DeleteCharacterUseCase.class),
                mock(CreateUserUseCase.class),
                mock(GetUserUseCase.class),
                mock(CreateDebateSessionUseCase.class),
                new GenerateNextTurnUseCase()
        );

        int exitCode = root.execute("debate", "create",
                "--owner-id", "1",
                "--topic-title", "부먹 vs 찍먹",
                "--format", "PROS_AND_CONS",
                "--max-rounds", "5",
                "--max-turn-length", "600");

        assertThat(exitCode).isNotZero();
        assertThat(text(err)).contains("Missing required option");
    }

    @Test
    void invalidParticipantJsonFailsBeforeUseCaseInvocation() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        installStreams(out, err);

        CreateDebateSessionUseCase createDebateSessionUseCase = mock(CreateDebateSessionUseCase.class);

        CommandLine root = commandLine(
                new RootCommand(),
                mock(CreateCharacterUseCase.class),
                mock(GetCharacterUseCase.class),
                mock(ListCharactersUseCase.class),
                mock(UpdateCharacterUseCase.class),
                mock(DeleteCharacterUseCase.class),
                mock(CreateUserUseCase.class),
                mock(GetUserUseCase.class),
                createDebateSessionUseCase,
                new GenerateNextTurnUseCase()
        );

        int exitCode = root.execute("--output", "TEXT", "debate", "create",
                "--owner-id", "1",
                "--topic-title", "부먹 vs 찍먹",
                "--format", "PROS_AND_CONS",
                "--max-rounds", "5",
                "--max-turn-length", "600",
                "--participant", "{invalid-json}",
                "--participant", "{\"characterId\":20,\"model\":\"FAST\"}");

        assertThat(exitCode).isEqualTo(2);
        assertThat(text(err)).contains("INVALID_ARGUMENT");
        verifyNoInteractions(createDebateSessionUseCase);
    }

    @Test
    void textOutputUsesTextFormatterForUserAndDebate() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        installStreams(out, err);

        CreateUserUseCase createUserUseCase = mock(CreateUserUseCase.class);
        when(createUserUseCase.execute(any())).thenReturn(userView());

        CreateDebateSessionUseCase createDebateSessionUseCase = mock(CreateDebateSessionUseCase.class);
        when(createDebateSessionUseCase.execute(any())).thenReturn(debateSessionView());

        CommandLine root = commandLine(
                new RootCommand(),
                mock(CreateCharacterUseCase.class),
                mock(GetCharacterUseCase.class),
                mock(ListCharactersUseCase.class),
                mock(UpdateCharacterUseCase.class),
                mock(DeleteCharacterUseCase.class),
                createUserUseCase,
                mock(GetUserUseCase.class),
                createDebateSessionUseCase,
                new GenerateNextTurnUseCase()
        );

        int userExitCode = root.execute("--output", "TEXT", "user", "create",
                "--email", "user@example.com",
                "--password", "password123",
                "--nickname", "마바라기");

        int debateExitCode = root.execute("--output", "TEXT", "debate", "create",
                "--owner-id", "1",
                "--topic-title", "부먹 vs 찍먹",
                "--topic-description", "어느 방식이 더 나은가?",
                "--topic-category", "FOOD",
                "--format", "PROS_AND_CONS",
                "--max-rounds", "5",
                "--max-turn-length", "600",
                "--participant", "{\"characterId\":10,\"model\":\"FAST\"}",
                "--participant", "{\"characterId\":20,\"model\":\"QUALITY\"}");

        assertThat(userExitCode).isZero();
        assertThat(debateExitCode).isZero();
        assertThat(text(out)).contains("user", "nickname=마바라기", "debate-session", "topicTitle=부먹 vs 찍먹");
    }

    @Test
    void debateNextTurnCommandOutputsCalculation() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        installStreams(out, err);

        CommandLine root = commandLine(
                new RootCommand(),
                mock(CreateCharacterUseCase.class),
                mock(GetCharacterUseCase.class),
                mock(ListCharactersUseCase.class),
                mock(UpdateCharacterUseCase.class),
                mock(DeleteCharacterUseCase.class),
                mock(CreateUserUseCase.class),
                mock(GetUserUseCase.class),
                mock(CreateDebateSessionUseCase.class),
                new GenerateNextTurnUseCase()
        );

        int exitCode = root.execute("--output", "TEXT", "debate", "next-turn",
                "--turn-index", "3",
                "--participant-count", "2",
                "--max-rounds", "5");

        assertThat(exitCode).isZero();
        assertThat(text(out)).contains(
                "debate-next-turn",
                "turnIndex=3",
                "speakerIndex=0",
                "round=2",
                "shouldCompleteSession=false"
        );
    }

    @Test
    void invalidCharacterIdFailsParsing() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        installStreams(out, err);

        CommandLine root = commandLine(
                new RootCommand(),
                mock(CreateCharacterUseCase.class),
                mock(GetCharacterUseCase.class),
                mock(ListCharactersUseCase.class),
                mock(UpdateCharacterUseCase.class),
                mock(DeleteCharacterUseCase.class),
                mock(CreateUserUseCase.class),
                mock(GetUserUseCase.class),
                mock(CreateDebateSessionUseCase.class),
                new GenerateNextTurnUseCase()
        );

        int exitCode = root.execute("character", "get", "abc");

        assertThat(exitCode).isNotZero();
        assertThat(text(err)).contains("Invalid value for positional parameter");
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
                mock(CreateCharacterUseCase.class),
                getCharacterUseCase,
                mock(ListCharactersUseCase.class),
                mock(UpdateCharacterUseCase.class),
                mock(DeleteCharacterUseCase.class),
                mock(CreateUserUseCase.class),
                mock(GetUserUseCase.class),
                mock(CreateDebateSessionUseCase.class),
                new GenerateNextTurnUseCase()
        );

        int exitCode = root.execute("character", "get", "99");

        assertThat(exitCode).isEqualTo(2);
        assertThat(text(err)).contains("CHARACTER_NOT_FOUND");
    }

    private static CommandLine commandLine(RootCommand rootCommand,
                                           CreateCharacterUseCase createCharacterUseCase,
                                           GetCharacterUseCase getCharacterUseCase,
                                           ListCharactersUseCase listCharactersUseCase,
                                           UpdateCharacterUseCase updateCharacterUseCase,
                                           DeleteCharacterUseCase deleteCharacterUseCase,
                                           CreateUserUseCase createUserUseCase,
                                           GetUserUseCase getUserUseCase,
                                           CreateDebateSessionUseCase createDebateSessionUseCase,
                                           GenerateNextTurnUseCase generateNextTurnUseCase) {
        ObjectMapper objectMapper = new ObjectMapper();

        CharacterCommand characterCommand = new CharacterCommand();
        CharacterCreateCommand characterCreateCommand = new CharacterCreateCommand(createCharacterUseCase, objectMapper);
        CharacterGetCommand characterGetCommand = new CharacterGetCommand(getCharacterUseCase, objectMapper);
        CharacterListCommand characterListCommand = new CharacterListCommand(listCharactersUseCase, objectMapper);
        CharacterUpdateCommand characterUpdateCommand = new CharacterUpdateCommand(updateCharacterUseCase, objectMapper);
        CharacterDeleteCommand characterDeleteCommand = new CharacterDeleteCommand(deleteCharacterUseCase, objectMapper);

        UserCommand userCommand = new UserCommand();
        UserCreateCommand userCreateCommand = new UserCreateCommand(createUserUseCase, objectMapper);
        UserGetCommand userGetCommand = new UserGetCommand(getUserUseCase, objectMapper);

        DebateCommand debateCommand = new DebateCommand();
        DebateCreateCommand debateCreateCommand = new DebateCreateCommand(createDebateSessionUseCase, objectMapper);
        DebateNextTurnCommand debateNextTurnCommand = new DebateNextTurnCommand(generateNextTurnUseCase, objectMapper);

        CommandLine root = new CommandLine(rootCommand);

        CommandLine character = new CommandLine(characterCommand);
        character.addSubcommand("create", characterCreateCommand);
        character.addSubcommand("get", characterGetCommand);
        character.addSubcommand("list", characterListCommand);
        character.addSubcommand("update", characterUpdateCommand);
        character.addSubcommand("delete", characterDeleteCommand);
        root.addSubcommand("character", character);

        CommandLine user = new CommandLine(userCommand);
        user.addSubcommand("create", userCreateCommand);
        user.addSubcommand("get", userGetCommand);
        root.addSubcommand("user", user);

        CommandLine debate = new CommandLine(debateCommand);
        debate.addSubcommand("create", debateCreateCommand);
        debate.addSubcommand("next-turn", debateNextTurnCommand);
        root.addSubcommand("debate", debate);

        root.setExecutionExceptionHandler((exception, commandLine, parseResult) -> {
            CliPrinter printer = new CliPrinter(objectMapper, System.out, System.err);
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
                PersonaFixtures.rationalGourmetJson(),
                "PRIVATE",
                LocalDateTime.of(2026, 6, 8, 12, 0),
                LocalDateTime.of(2026, 6, 8, 12, 0)
        );
    }

    private static UserView userView() {
        return new UserView(
                1L,
                "user@example.com",
                "마바라기",
                LocalDateTime.of(2026, 6, 8, 12, 0)
        );
    }

    private static DebateSessionView debateSessionView() {
        return new DebateSessionView(
                1L,
                1L,
                "부먹 vs 찍먹",
                "어느 방식이 더 나은가?",
                "FOOD",
                DebateSessionStatus.CREATED,
                DebateFormat.PROS_AND_CONS,
                5,
                0,
                600,
                List.of(
                        debateParticipantView(10L, 0, ParticipantModel.FAST, "합리주의 미식가"),
                        debateParticipantView(20L, 1, ParticipantModel.QUALITY, "감성주의 미식가")
                ),
                LocalDateTime.of(2026, 6, 8, 12, 0)
        );
    }

    private static DebateParticipantView debateParticipantView(Long sourceCharacterId,
                                                               int position,
                                                               ParticipantModel model,
                                                               String name) {
        return new DebateParticipantView(
                (long) (position + 1),
                sourceCharacterId,
                position,
                model,
                name,
                "논리적인 캐릭터",
                PersonaFixtures.rationalGourmetJson()
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
