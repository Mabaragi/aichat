package com.example.aichat.cli.user;

import com.example.aichat.cli.CliPrinter;
import com.example.aichat.user.application.GetUserUseCase;
import com.example.aichat.user.application.UserView;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Component
@Command(
        name = "get",
        mixinStandardHelpOptions = true,
        description = "Get one user by user ID."
)
public class UserGetCommand implements Runnable {

    private final GetUserUseCase getUserUseCase;
    private final ObjectMapper objectMapper;

    @ParentCommand
    private UserCommand userCommand;

    @Parameters(index = "0", paramLabel = "USER_ID", description = "User ID.")
    private Long userId;

    public UserGetCommand(GetUserUseCase getUserUseCase,
                          ObjectMapper objectMapper) {
        this.getUserUseCase = getUserUseCase;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run() {
        UserView result = getUserUseCase.execute(userId);
        new CliPrinter(objectMapper, System.out, System.err)
                .printSuccess(userCommand.rootCommand().outputMode(), result);
    }
}
