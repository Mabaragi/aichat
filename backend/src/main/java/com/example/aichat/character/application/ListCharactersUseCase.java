package com.example.aichat.character.application;

import com.example.aichat.character.domain.CharacterRepository;
import com.example.aichat.common.security.RequestActor;
import org.springframework.stereotype.Service;

@Service
public class ListCharactersUseCase {

    private final CharacterRepository characterRepository;

    public ListCharactersUseCase(CharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
    }

    public ListCharactersResult execute(Long ownerId) {
        return execute(RequestActor.system(), ownerId);
    }

    public ListCharactersResult execute(RequestActor actor, Long ownerId) {
        return new ListCharactersResult(characterRepository.findByOwnerId(ownerId)
                .stream()
                .filter(character -> "PUBLIC".equals(character.getVisibility())
                        || actor.canManage(character.getOwnerId()))
                .map(CharacterView::from)
                .toList());
    }
}
