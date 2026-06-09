package com.example.aichat.character.infrastructure;

import com.example.aichat.character.domain.Character;
import com.example.aichat.character.domain.CharacterRepository;
import com.example.aichat.character.domain.Personality;
import com.example.aichat.character.domain.SpeechStyle;
import org.springframework.context.annotation.Profile;
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
    public void deleteById(Long characterId) {
        characterJpaRepository.deleteById(characterId);
    }

    private static CharacterJpaEntity toEntity(Character character) {
        return new CharacterJpaEntity(
                character.getId(),
                character.getOwnerId(),
                character.getName(),
                character.getDescription(),
                unwrap(character.getPersonality()),
                unwrap(character.getSpeechStyle()),
                character.getVisibility(),
                character.getCreatedAt(),
                character.getUpdatedAt()
        );
    }

    private static Character toDomain(CharacterJpaEntity entity) {
        return new Character(
                entity.id(),
                entity.ownerId(),
                entity.name(),
                entity.description(),
                wrapPersonality(entity.personality()),
                wrapSpeechStyle(entity.speechStyle()),
                entity.visibility(),
                entity.createdAt(),
                entity.updatedAt()
        );
    }

    private static String unwrap(Personality personality) {
        return personality == null ? null : personality.value();
    }

    private static String unwrap(SpeechStyle speechStyle) {
        return speechStyle == null ? null : speechStyle.value();
    }

    private static Personality wrapPersonality(String personality) {
        return personality == null ? null : Personality.of(personality);
    }

    private static SpeechStyle wrapSpeechStyle(String speechStyle) {
        return speechStyle == null ? null : SpeechStyle.of(speechStyle);
    }

}
