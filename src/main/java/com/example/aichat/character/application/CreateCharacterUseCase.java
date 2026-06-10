package com.example.aichat.character.application;

import com.example.aichat.character.domain.Character;
import com.example.aichat.character.domain.CharacterRepository;
import com.example.aichat.common.exception.BusinessException;
import com.example.aichat.common.exception.ErrorCode;
import com.example.aichat.common.time.TimeProvider;
import org.springframework.stereotype.Service;

@Service
public class CreateCharacterUseCase {

    private final CharacterRepository characterRepository;
    private final TimeProvider timeProvider;

    public CreateCharacterUseCase(CharacterRepository characterRepository,
                                  TimeProvider timeProvider) {
        this.characterRepository = characterRepository;
        this.timeProvider = timeProvider;
    }

    public CharacterView execute(CreateCharacterCommand command) {
        if (!command.actor().canManage(command.ownerId())) {
            throw inaccessible(command.ownerId());
        }
        var now = timeProvider.now();
        Character character = Character.create(
                command.ownerId(),
                command.name(),
                command.description(),
                command.personality(),
                command.speechStyle(),
                command.visibility(),
                now,
                now
        );

        return CharacterView.from(characterRepository.save(character));
    }

    private static BusinessException inaccessible(Long ownerId) {
        return new BusinessException(
                ErrorCode.USER_NOT_FOUND,
                "User not found: " + ownerId
        );
    }
}
