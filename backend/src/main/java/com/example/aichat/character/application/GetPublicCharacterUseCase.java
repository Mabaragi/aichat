package com.example.aichat.character.application;

import com.example.aichat.category.application.CategoryResolver;
import com.example.aichat.category.application.CategorySummaryView;
import com.example.aichat.character.domain.Character;
import com.example.aichat.character.domain.CharacterRepository;
import com.example.aichat.common.exception.BusinessException;
import com.example.aichat.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GetPublicCharacterUseCase {

    private final CharacterRepository characterRepository;
    private final CategoryResolver categoryResolver;

    public GetPublicCharacterUseCase(CharacterRepository characterRepository,
                                     CategoryResolver categoryResolver) {
        this.characterRepository = characterRepository;
        this.categoryResolver = categoryResolver;
    }

    @Transactional(readOnly = true)
    public CharacterView execute(Long characterId) {
        Character character = characterRepository.findById(characterId)
                .filter(found -> "PUBLIC".equals(found.getVisibility()))
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.CHARACTER_NOT_FOUND,
                        "Character not found: " + characterId
                ));
        CategorySummaryView category = character.getCategoryId() == null
                ? null
                : categoryResolver.summariesById(List.of(character.getCategoryId()))
                .get(character.getCategoryId());
        return CharacterView.from(character, category);
    }
}
