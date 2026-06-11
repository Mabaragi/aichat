package com.example.aichat.character.infrastructure;

import com.example.aichat.character.domain.Character;
import com.example.aichat.character.domain.CharacterRepository;
import com.example.aichat.character.domain.Persona;
import com.example.aichat.common.application.PagedResult;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile("!dev")
public class JpaCharacterRepositoryAdapter implements CharacterRepository {

    private final CharacterJpaRepository characterJpaRepository;

    public JpaCharacterRepositoryAdapter(CharacterJpaRepository characterJpaRepository) {
        this.characterJpaRepository = characterJpaRepository;
    }

    @Override
    public Character save(Character character) {
        CharacterJpaEntity persisted = characterJpaRepository.save(toEntity(character));
        return toDomain(persisted);
    }

    @Override
    public Optional<Character> findById(Long characterId) {
        return characterJpaRepository.findById(characterId)
                .map(JpaCharacterRepositoryAdapter::toDomain);
    }

    @Override
    public List<Character> findByOwnerId(Long ownerId) {
        return characterJpaRepository.findByOwnerId(ownerId)
                .stream()
                .map(JpaCharacterRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    public PagedResult<Character> findPublic(String query, Long categoryId, int page, int size) {
        var result = characterJpaRepository.findPublic(
                normalizeQuery(query),
                categoryId,
                PageRequest.of(page, size)
        );
        return new PagedResult<>(
                result.getContent().stream()
                        .map(JpaCharacterRepositoryAdapter::toDomain)
                        .toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext()
        );
    }

    @Override
    public void deleteById(Long characterId) {
        characterJpaRepository.deleteById(characterId);
    }

    private static CharacterJpaEntity toEntity(Character character) {
        return new CharacterJpaEntity(
                character.getId(),
                character.getOwnerId(),
                character.getCategoryId(),
                character.getName(),
                character.getDescription(),
                unwrap(character.getPersona()),
                character.getVisibility(),
                character.getCreatedAt(),
                character.getUpdatedAt()
        );
    }

    private static Character toDomain(CharacterJpaEntity entity) {
        return new Character(
                entity.id(),
                entity.ownerId(),
                entity.categoryId(),
                entity.name(),
                entity.description(),
                wrapPersona(entity.persona()),
                entity.visibility(),
                entity.createdAt(),
                entity.updatedAt()
        );
    }

    private static String unwrap(Persona persona) {
        return persona == null ? null : persona.value();
    }

    private static Persona wrapPersona(String persona) {
        return persona == null ? null : Persona.of(persona);
    }

    private static String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        return query.trim();
    }

}
