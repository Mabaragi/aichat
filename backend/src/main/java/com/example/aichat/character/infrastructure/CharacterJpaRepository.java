package com.example.aichat.character.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CharacterJpaRepository extends JpaRepository<CharacterJpaEntity, Long> {

    List<CharacterJpaEntity> findByOwnerId(Long id);

    @Query("""
            select character from CharacterJpaEntity character
            where character.visibility = 'PUBLIC'
              and (:categoryId is null or character.categoryId = :categoryId)
              and (
                :query is null
                or lower(character.name) like lower(concat('%', :query, '%'))
                or lower(coalesce(character.description, '')) like lower(concat('%', :query, '%'))
              )
            order by character.updatedAt desc, character.id desc
            """)
    Page<CharacterJpaEntity> findPublic(String query, Long categoryId, Pageable pageable);
}
