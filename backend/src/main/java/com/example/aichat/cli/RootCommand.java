package com.example.aichat.cli;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

@Component
@Command(
        name = "aichat",
        mixinStandardHelpOptions = true,
        version = "0.0.1",
        description = {
                "AI debate platform CLI.",
                "Use character, user, and debate commands directly from the terminal."
        }
)
public class RootCommand implements Runnable {

    @Option(names = {"-o", "--output"}, defaultValue = "JSON",
            description = "Output mode: JSON or TEXT.")
    private OutputMode outputMode;

    @Spec
    private CommandSpec spec;

    @Override
    public void run() {
        spec.commandLine().usage(System.out);
    }

    public OutputMode outputMode() {
        return outputMode;
    }
}
