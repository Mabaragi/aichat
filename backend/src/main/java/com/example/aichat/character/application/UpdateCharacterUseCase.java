package com.example.aichat.character.application;

import com.example.aichat.category.application.CategoryResolver;
import com.example.aichat.category.application.CategorySummaryView;
import com.example.aichat.category.domain.Category;
import com.example.aichat.category.domain.CategoryScope;
import com.example.aichat.character.domain.Character;
import com.example.aichat.character.domain.CharacterRepository;
import com.example.aichat.common.exception.BusinessException;
import com.example.aichat.common.exception.ErrorCode;
import com.example.aichat.common.time.TimeProvider;
import org.springframework.stereotype.Service;

@Service
public class UpdateCharacterUseCase {

    private final CharacterRepository characterRepository;
    private final CategoryResolver categoryResolver;
    private final TimeProvider timeProvider;

    public UpdateCharacterUseCase(CharacterRepository characterRepository,
                                  CategoryResolver categoryResolver,
                                  TimeProvider timeProvider) {
        this.characterRepository = characterRepository;
        this.categoryResolver = categoryResolver;
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

        Category category = command.category() == null
                ? null
                : categoryResolver.requireActive(CategoryScope.CHARACTER, command.category());
        Long nextCategoryId = category == null ? character.getCategoryId() : category.getId();

        character.update(
                nextCategoryId,
                command.name() != null ? command.name() : character.getName(),
                command.description() != null ? command.description() : character.getDescription(),
                command.persona() != null ? command.persona() : character.getPersona(),
                command.visibility() != null ? command.visibility() : character.getVisibility(),
                timeProvider.now()
        );

        Character saved = characterRepository.save(character);
        CategorySummaryView summary = category == null
                ? saved.getCategoryId() == null
                ? null
                : categoryResolver.summariesById(java.util.List.of(saved.getCategoryId()))
                .get(saved.getCategoryId())
                : CategorySummaryView.from(category);
        return CharacterView.from(saved, summary);
    }
}
