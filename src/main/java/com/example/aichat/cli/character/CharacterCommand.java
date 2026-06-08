package com.example.aichat.cli.character;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

@Component
@Command(
        name = "character",
        mixinStandardHelpOptions = true,
        description = {
                "Character management commands.",
                "Create, inspect, list, update, and delete AI characters."
        }
)
public class CharacterCommand implements Runnable {

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
