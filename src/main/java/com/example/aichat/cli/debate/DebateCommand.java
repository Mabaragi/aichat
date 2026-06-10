package com.example.aichat.cli.debate;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

@Component
@Command(
        name = "debate",
        mixinStandardHelpOptions = true,
        description = {
                "Debate commands.",
                "Create debate sessions and inspect next-turn calculations."
        }
)
public class DebateCommand implements Runnable {

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
