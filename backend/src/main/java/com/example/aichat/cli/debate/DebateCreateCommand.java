package com.example.aichat.cli.debate;

import com.example.aichat.cli.CliPrinter;
import com.example.aichat.debate.application.CreateDebateParticipantCommand;
import com.example.aichat.debate.application.CreateDebateSessionCommand;
import com.example.aichat.debate.application.CreateDebateSessionUseCase;
import com.example.aichat.debate.application.DebateSessionView;
import com.example.aichat.debate.domain.DebateFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.util.List;

@Component
@Command(
        name = "create",
        mixinStandardHelpOptions = true,
        description = "Create a debate session."
)
public class DebateCreateCommand implements Runnable {

    private final CreateDebateSessionUseCase createDebateSessionUseCase;
    private final ObjectMapper objectMapper;

    @ParentCommand
    private DebateCommand debateCommand;

    @Option(names = "--owner-id", required = true, description = "Owner user ID.")
    private Long ownerId;

    @Option(names = "--topic-title", required = true, description = "Debate topic title.")
    private String topicTitle;

    @Option(names = "--topic-description", description = "Debate topic description.")
    private String topicDescription;

    @Option(names = "--topic-category", description = "Debate topic category.")
    private String topicCategory;

    @Option(names = "--format", required = true, description = "Debate format such as FREE_DISCUSSION or PROS_AND_CONS.")
    private DebateFormat format;

    @Option(names = "--max-rounds", required = true, description = "Maximum debate rounds.")
    private int maxRounds;

    @Option(names = "--max-turn-length", required = true, description = "Maximum turn length.")
    private Integer maxTurnLength;

    @Option(names = "--participant", required = true, description = "Participant JSON payload. Repeat exactly twice.")
    private List<String> participantPayloads;

    public DebateCreateCommand(CreateDebateSessionUseCase createDebateSessionUseCase,
                               ObjectMapper objectMapper) {
        this.createDebateSessionUseCase = createDebateSessionUseCase;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run() {
        List<CreateDebateParticipantCommand> participants = DebateCommandSupport.toParticipantCommands(
                objectMapper,
                participantPayloads
        );

        DebateSessionView result = createDebateSessionUseCase.execute(new CreateDebateSessionCommand(
                ownerId,
                topicTitle,
                topicDescription,
                topicCategory,
                format,
                maxRounds,
                maxTurnLength,
                participants
        ));

        new CliPrinter(objectMapper, System.out, System.err)
                .printSuccess(debateCommand.rootCommand().outputMode(), result);
    }
}
