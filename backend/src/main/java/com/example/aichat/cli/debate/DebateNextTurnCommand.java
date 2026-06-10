package com.example.aichat.cli.debate;

import com.example.aichat.cli.CliPrinter;
import com.example.aichat.debate.application.GenerateNextTurnUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Component
@Command(
        name = "next-turn",
        mixinStandardHelpOptions = true,
        description = "Calculate the next debate turn."
)
public class DebateNextTurnCommand implements Runnable {

    private final GenerateNextTurnUseCase generateNextTurnUseCase;
    private final ObjectMapper objectMapper;

    @ParentCommand
    private DebateCommand debateCommand;

    @Option(names = "--turn-index", required = true, description = "Next turn index, starting at 1.")
    private int turnIndex;

    @Option(names = "--participant-count", required = true, description = "Number of debate participants.")
    private int participantCount;

    @Option(names = "--max-rounds", required = true, description = "Maximum debate rounds.")
    private int maxRounds;

    public DebateNextTurnCommand(GenerateNextTurnUseCase generateNextTurnUseCase,
                                 ObjectMapper objectMapper) {
        this.generateNextTurnUseCase = generateNextTurnUseCase;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run() {
        new CliPrinter(objectMapper, System.out, System.err)
                .printSuccess(debateCommand.rootCommand().outputMode(), new DebateNextTurnResult(
                        turnIndex,
                        participantCount,
                        maxRounds,
                        generateNextTurnUseCase.resolveSpeakerIndex(turnIndex, participantCount),
                        generateNextTurnUseCase.resolveRound(turnIndex, participantCount),
                        generateNextTurnUseCase.shouldCompleteSession(turnIndex, maxRounds, participantCount)
                ));
    }
}
