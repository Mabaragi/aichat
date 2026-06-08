package com.example.aichat.character.infrastructure;

import com.example.aichat.character.domain.CharacterRepository;

class InMemoryCharacterRepositoryTest extends CharacterRepositoryContractTest {

    private final CharacterRepository repository = new InMemoryCharacterRepository();

    @Override
    protected CharacterRepository repository() {
        return repository;
    }
}
