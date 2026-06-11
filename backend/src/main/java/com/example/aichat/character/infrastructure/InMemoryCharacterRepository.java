package com.example.aichat.character.infrastructure;

import com.example.aichat.character.domain.Character;
import com.example.aichat.character.domain.CharacterRepository;
import com.example.aichat.common.application.PagedResult;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@Profile("dev")
public class InMemoryCharacterRepository implements CharacterRepository {

    private final ConcurrentHashMap<Long, Character> storage = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0L);

    @Override
    public Character save(Character character) {
        Character persisted = character.getId() == null
                ? new Character(
                sequence.incrementAndGet(),
                character.getOwnerId(),
                character.getCategoryId(),
                character.getName(),
                character.getDescription(),
                character.getPersona(),
                character.getVisibility(),
                character.getCreatedAt(),
                character.getUpdatedAt())
                : character;

        storage.put(persisted.getId(), persisted);
        return persisted;
    }

    @Override
    public Optional<Character> findById(Long characterId) {
        return Optional.ofNullable(storage.get(characterId));
    }

    @Override
    public List<Character> findByOwnerId(Long ownerId) {
        return storage.values().stream()
                .filter(character -> character.getOwnerId().equals(ownerId))
                .sorted(Comparator.comparing(Character::getId))
                .toList();
    }

    @Override
    public PagedResult<Character> findPublic(String query, Long categoryId, int page, int size) {
        List<Character> filtered = storage.values().stream()
                .filter(character -> "PUBLIC".equals(character.getVisibility()))
                .filter(character -> categoryId == null || categoryId.equals(character.getCategoryId()))
                .filter(character -> matches(query, character))
                .sorted(Comparator.comparing(Character::getUpdatedAt).reversed()
                        .thenComparing(Comparator.comparing(Character::getId).reversed()))
                .toList();
        int from = Math.min(page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        List<Character> items = filtered.subList(from, to);
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) filtered.size() / size);
        return new PagedResult<>(items, page, size, filtered.size(), totalPages, to < filtered.size());
    }

    @Override
    public void deleteById(Long characterId) {
        storage.remove(characterId);
    }

    private static boolean matches(String query, Character character) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String normalized = query.toLowerCase(java.util.Locale.ROOT);
        return character.getName().toLowerCase(java.util.Locale.ROOT).contains(normalized)
                || (character.getDescription() != null
                && character.getDescription().toLowerCase(java.util.Locale.ROOT).contains(normalized));
    }
}
