package com.example.aichat.character.application;

import com.example.aichat.category.application.CategoryResolver;
import com.example.aichat.category.application.CategorySummaryView;
import com.example.aichat.character.domain.CharacterRepository;
import com.example.aichat.common.security.RequestActor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Service
public class ListCharactersUseCase {

    private final CharacterRepository characterRepository;
    private final CategoryResolver categoryResolver;

    public ListCharactersUseCase(CharacterRepository characterRepository,
                                 CategoryResolver categoryResolver) {
        this.characterRepository = characterRepository;
        this.categoryResolver = categoryResolver;
    }

    public ListCharactersResult execute(Long ownerId) {
        return execute(RequestActor.system(), ownerId);
    }

    public ListCharactersResult execute(RequestActor actor, Long ownerId) {
        var visible = characterRepository.findByOwnerId(ownerId)
                .stream()
                .filter(character -> "PUBLIC".equals(character.getVisibility())
                        || actor.canManage(character.getOwnerId()))
                .toList();
        Map<Long, CategorySummaryView> categories = categoryResolver.summariesById(
                visible.stream().map(character -> character.getCategoryId())
                        .filter(Objects::nonNull)
                        .toList()
        );
        return new ListCharactersResult(visible.stream()
                .map(character -> CharacterView.from(character, categories.get(character.getCategoryId())))
                .toList());
    }
}
