package com.example.aichat.character.application;

import com.example.aichat.category.application.CategoryResolver;
import com.example.aichat.category.application.CategorySummaryView;
import com.example.aichat.character.domain.Character;
import com.example.aichat.character.domain.CharacterRepository;
import com.example.aichat.common.exception.BusinessException;
import com.example.aichat.common.exception.ErrorCode;
import com.example.aichat.common.security.RequestActor;
import org.springframework.stereotype.Service;

@Service
public class GetCharacterUseCase {

    private final CharacterRepository characterRepository;
    private final CategoryResolver categoryResolver;

    public GetCharacterUseCase(CharacterRepository characterRepository,
                               CategoryResolver categoryResolver) {
        this.characterRepository = characterRepository;
        this.categoryResolver = categoryResolver;
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
        CategorySummaryView category = character.getCategoryId() == null
                ? null
                : categoryResolver.summariesById(java.util.List.of(character.getCategoryId()))
                .get(character.getCategoryId());
        return CharacterView.from(character, category);
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
