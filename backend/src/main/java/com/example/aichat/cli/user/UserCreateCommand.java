package com.example.aichat.cli.user;

import com.example.aichat.cli.CliPrinter;
import com.example.aichat.user.application.CreateUserCommand;
import com.example.aichat.user.application.CreateUserUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Component
@Command(
        name = "create",
        mixinStandardHelpOptions = true,
        description = "Create a user."
)
public class UserCreateCommand implements Runnable {

    private final CreateUserUseCase createUserUseCase;
    private final ObjectMapper objectMapper;

    @ParentCommand
    private UserCommand userCommand;

    @Option(names = "--email", required = true, description = "User email.")
    private String email;

    @Option(names = "--password", required = true, description = "Raw password.")
    private String password;

    @Option(names = "--nickname", required = true, description = "User nickname.")
    private String nickname;

    public UserCreateCommand(CreateUserUseCase createUserUseCase,
                             ObjectMapper objectMapper) {
        this.createUserUseCase = createUserUseCase;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run() {
        new CliPrinter(objectMapper, System.out, System.err)
                .printSuccess(userCommand.rootCommand().outputMode(),
                        createUserUseCase.execute(new CreateUserCommand(email, password, nickname)));
    }
}
