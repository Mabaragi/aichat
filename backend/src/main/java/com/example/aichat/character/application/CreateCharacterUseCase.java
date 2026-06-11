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
public class CreateCharacterUseCase {

    private final CharacterRepository characterRepository;
    private final CategoryResolver categoryResolver;
    private final TimeProvider timeProvider;

    public CreateCharacterUseCase(CharacterRepository characterRepository,
                                  CategoryResolver categoryResolver,
                                  TimeProvider timeProvider) {
        this.characterRepository = characterRepository;
        this.categoryResolver = categoryResolver;
        this.timeProvider = timeProvider;
    }

    public CharacterView execute(CreateCharacterCommand command) {
        if (!command.actor().canManage(command.ownerId())) {
            throw inaccessible(command.ownerId());
        }
        var now = timeProvider.now();
        Category category = categoryResolver.requireActive(CategoryScope.CHARACTER, command.category());
        Character character = Character.create(
                command.ownerId(),
                category.getId(),
                command.name(),
                command.description(),
                command.personality(),
                command.speechStyle(),
                command.visibility(),
                now,
                now
        );

        return CharacterView.from(
                characterRepository.save(character),
                CategorySummaryView.from(category)
        );
    }

    private static BusinessException inaccessible(Long ownerId) {
        return new BusinessException(
                ErrorCode.USER_NOT_FOUND,
                "User not found: " + ownerId
        );
    }
}
