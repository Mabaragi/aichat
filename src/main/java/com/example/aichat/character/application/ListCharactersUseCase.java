package com.example.aichat.character.application;

import com.example.aichat.character.domain.CharacterRepository;
import org.springframework.stereotype.Service;

@Service
public class ListCharactersUseCase {

    private final CharacterRepository characterRepository;

    public ListCharactersUseCase(CharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
    }

    public ListCharactersResult execute(Long ownerId) {
        return new ListCharactersResult(characterRepository.findByOwnerId(ownerId)
                .stream()
                .map(CharacterView::from)
                .toList());
    }
}
