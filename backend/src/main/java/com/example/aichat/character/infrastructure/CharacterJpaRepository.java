package com.example.aichat.character.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CharacterJpaRepository extends JpaRepository<CharacterJpaEntity, Long> {

    List<CharacterJpaEntity> findByOwnerId(Long id);
}
