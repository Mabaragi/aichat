package com.example.aichat.cli.user;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

@Component
@Command(
        name = "user",
        mixinStandardHelpOptions = true,
        description = {
                "User commands.",
                "Create and inspect users from the terminal."
        }
)
public class UserCommand implements Runnable {

    @ParentCommand
    private com.example.aichat.cli.RootCommand rootCommand;

    @Spec
    private CommandSpec spec;

    @Override
    public void run() {
        spec.commandLine().usage(System.out);
    }

    public com.example.aichat.cli.RootCommand rootCommand() {
        return rootCommand;
    }
}
