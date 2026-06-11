package com.example.aichat.character.domain;

import com.example.aichat.common.application.PagedResult;

import java.util.List;
import java.util.Optional;

public interface CharacterRepository {

    Character save(Character character);

    Optional<Character> findById(Long characterId);

    List<Character> findByOwnerId(Long ownerId);

    default PagedResult<Character> findPublic(String query, Long categoryId, int page, int size) {
        return new PagedResult<>(List.of(), page, size, 0, 0, false);
    }

    void deleteById(Long characterId);
}
