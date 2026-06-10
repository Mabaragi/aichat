package com.example.aichat.character.application;

import com.example.aichat.character.domain.Character;
import com.example.aichat.character.domain.CharacterRepository;
import com.example.aichat.common.exception.BusinessException;
import com.example.aichat.common.exception.ErrorCode;
import com.example.aichat.common.time.TimeProvider;
import org.springframework.stereotype.Service;

@Service
public class UpdateCharacterUseCase {

    private final CharacterRepository characterRepository;
    private final TimeProvider timeProvider;

    public UpdateCharacterUseCase(CharacterRepository characterRepository,
                                  TimeProvider timeProvider) {
        this.characterRepository = characterRepository;
        this.timeProvider = timeProvider;
    }

    public CharacterView execute(UpdateCharacterCommand command) {
        Character character = characterRepository.findById(command.characterId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.CHARACTER_NOT_FOUND,
                        "Character not found: " + command.characterId()
                ));

        if (!command.actor().canManage(character.getOwnerId())) {
            throw new BusinessException(
                    ErrorCode.CHARACTER_NOT_FOUND,
                    "Character not found: " + command.characterId()
            );
        }

        character.update(
                command.name() != null ? command.name() : character.getName(),
                command.description() != null ? command.description() : character.getDescription(),
                command.personality() != null ? command.personality() : character.getPersonality(),
                command.speechStyle() != null ? command.speechStyle() : character.getSpeechStyle(),
                command.visibility() != null ? command.visibility() : character.getVisibility(),
                timeProvider.now()
        );

        return CharacterView.from(characterRepository.save(character));
    }
}
