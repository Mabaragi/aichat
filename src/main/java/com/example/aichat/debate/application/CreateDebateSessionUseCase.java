package com.example.aichat.debate.application;

import com.example.aichat.character.domain.Character;
import com.example.aichat.character.domain.CharacterRepository;
import com.example.aichat.character.domain.Personality;
import com.example.aichat.character.domain.SpeechStyle;
import com.example.aichat.common.exception.BusinessException;
import com.example.aichat.common.exception.ErrorCode;
import com.example.aichat.common.time.TimeProvider;
import com.example.aichat.debate.domain.DebateParticipant;
import com.example.aichat.debate.domain.DebateSession;
import com.example.aichat.debate.domain.DebateSessionRepository;
import com.example.aichat.user.domain.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Service
public class CreateDebateSessionUseCase {

    private final UserRepository userRepository;
    private final CharacterRepository characterRepository;
    private final DebateSessionRepository debateSessionRepository;
    private final TimeProvider timeProvider;

    public CreateDebateSessionUseCase(UserRepository userRepository,
                                      CharacterRepository characterRepository,
                                      DebateSessionRepository debateSessionRepository,
                                      TimeProvider timeProvider) {
        this.userRepository = userRepository;
        this.characterRepository = characterRepository;
        this.debateSessionRepository = debateSessionRepository;
        this.timeProvider = timeProvider;
    }

    @Transactional
    public DebateSessionView execute(CreateDebateSessionCommand command) {

        requireUser(command.ownerId());
        List<DebateParticipant> participants = createParticipants(command);

        DebateSession session = DebateSession.create(
                command.ownerId(),
                command.topicTitle(),
                command.topicDescription(),
                command.topicCategory(),
                command.format(),
                command.maxRounds(),
                command.maxTurnLength(),
                participants,
                timeProvider.now()
        );

        return DebateSessionView.from(debateSessionRepository.save(session));
    }

    private void requireUser(Long ownerId) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.USER_NOT_FOUND,
                        "User not found: " + ownerId
                ));
    }

    private List<DebateParticipant> createParticipants(CreateDebateSessionCommand command) {
        if (command.participants() == null) {
            throw new IllegalArgumentException("participants are required");
        }

        return IntStream.range(0, command.participants().size())
                .mapToObj(position -> createParticipant(
                        command.ownerId(),
                        position,
                        command.participants().get(position)
                ))
                .toList();
    }

    private DebateParticipant createParticipant(Long ownerId, int position,
                                                CreateDebateParticipantCommand command) {
        Character character = characterRepository.findById(command.characterId())
                .filter(found -> canUse(ownerId, found))
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.CHARACTER_NOT_FOUND,
                        "Character not found or inaccessible: " + command.characterId()
                ));

        return DebateParticipant.create(
                character.getId(),
                position,
                command.model(),
                character.getName(),
                character.getDescription(),
                unwrap(character.getPersonality()),
                unwrap(character.getSpeechStyle())
        );
    }

    private static boolean canUse(Long ownerId, Character character) {
        return character.getOwnerId().equals(ownerId)
                || "PUBLIC".equals(character.getVisibility());
    }

    private static String unwrap(Personality personality) {
        return personality == null ? null : personality.value();
    }

    private static String unwrap(SpeechStyle speechStyle) {
        return speechStyle == null ? null : speechStyle.value();
    }
}
