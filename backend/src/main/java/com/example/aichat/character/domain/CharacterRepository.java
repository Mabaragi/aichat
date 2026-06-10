package com.example.aichat.character.domain;

import java.util.List;
import java.util.Optional;

public interface CharacterRepository {

    Character save(Character character);

    Optional<Character> findById(Long characterId);

    List<Character> findByOwnerId(Long ownerId);

    void deleteById(Long characterId);
}
