package com.example.aichat.character.application;

import com.example.aichat.character.domain.Character;
import com.example.aichat.character.domain.CharacterRepository;
import com.example.aichat.common.exception.BusinessException;
import com.example.aichat.common.exception.ErrorCode;
import com.example.aichat.common.security.RequestActor;
import org.springframework.stereotype.Service;

@Service
public class GetCharacterUseCase {

    private final CharacterRepository characterRepository;

    public GetCharacterUseCase(CharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
    }

    public CharacterView execute(Long characterId) {
        return execute(RequestActor.system(), characterId);
    }

    public CharacterView execute(RequestActor actor, Long characterId) {
        Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.CHARACTER_NOT_FOUND,
                        "Character not found: " + characterId
                ));
        if (!isPublic(character) && !actor.canManage(character.getOwnerId())) {
            throw notFound(characterId);
        }
        return CharacterView.from(character);
    }

    private static boolean isPublic(Character character) {
        return "PUBLIC".equals(character.getVisibility());
    }

    private static BusinessException notFound(Long characterId) {
        return new BusinessException(
                ErrorCode.CHARACTER_NOT_FOUND,
                "Character not found: " + characterId
        );
    }
}
