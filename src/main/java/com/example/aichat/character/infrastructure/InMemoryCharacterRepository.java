package com.example.aichat.character.infrastructure;

import com.example.aichat.character.domain.Character;
import com.example.aichat.character.domain.CharacterRepository;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryCharacterRepository implements CharacterRepository {

    private final ConcurrentHashMap<Long, Character> storage = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0L);

    @Override
    public Character save(Character character) {
        Character persisted = character.getId() == null
                ? new Character(
                sequence.incrementAndGet(),
                character.getOwnerId(),
                character.getName(),
                character.getDescription(),
                character.getPersonality(),
                character.getSpeechStyle(),
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
    public void deleteById(Long characterId) {
        storage.remove(characterId);
    }
}
