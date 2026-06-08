package com.example.aichat.character.application;

import com.example.aichat.character.domain.CharacterRepository;
import com.example.aichat.common.exception.BusinessException;
import com.example.aichat.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class DeleteCharacterUseCase {

    private final CharacterRepository characterRepository;

    public DeleteCharacterUseCase(CharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
    }

    public DeleteCharacterResult execute(Long characterId) {
        characterRepository.findById(characterId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.CHARACTER_NOT_FOUND,
                        "Character not found: " + characterId
                ));

        characterRepository.deleteById(characterId);
        return new DeleteCharacterResult(characterId);
    }
}
