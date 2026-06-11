package com.example.aichat.character.application;

import com.example.aichat.category.application.CategoryResolver;
import com.example.aichat.category.application.CategorySummaryView;
import com.example.aichat.category.domain.Category;
import com.example.aichat.category.domain.CategoryScope;
import com.example.aichat.character.domain.Character;
import com.example.aichat.character.domain.CharacterRepository;
import com.example.aichat.common.application.PagedResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;

@Service
public class ListPublicCharactersUseCase {

    private final CharacterRepository characterRepository;
    private final CategoryResolver categoryResolver;

    public ListPublicCharactersUseCase(CharacterRepository characterRepository,
                                       CategoryResolver categoryResolver) {
        this.characterRepository = characterRepository;
        this.categoryResolver = categoryResolver;
    }

    @Transactional(readOnly = true)
    public PagedResult<CharacterView> execute(String query, String categorySlug,
                                              int page, int size) {
        Category category = categorySlug == null || categorySlug.isBlank()
                ? null
                : categoryResolver.requireActive(CategoryScope.CHARACTER, categorySlug);
        PagedResult<Character> result = characterRepository.findPublic(
                query,
                category == null ? null : category.getId(),
                page,
                size
        );
        Map<Long, CategorySummaryView> categories = categoryResolver.summariesById(
                result.items().stream()
                        .map(Character::getCategoryId)
                        .filter(Objects::nonNull)
                        .toList()
        );
        return new PagedResult<>(
                result.items().stream()
                        .map(character -> CharacterView.from(
                                character,
                                categories.get(character.getCategoryId())
                        ))
                        .toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext()
        );
    }
}
