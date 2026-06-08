package com.example.aichat.character.application;

import com.example.aichat.character.domain.CharacterRepository;
import com.example.aichat.common.exception.BusinessException;
import com.example.aichat.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class GetCharacterUseCase {

    private final CharacterRepository characterRepository;

    public GetCharacterUseCase(CharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
    }

    public CharacterView execute(Long characterId) {
        return CharacterView.from(characterRepository.findById(characterId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.CHARACTER_NOT_FOUND,
                        "Character not found: " + characterId
                )));
    }
}
