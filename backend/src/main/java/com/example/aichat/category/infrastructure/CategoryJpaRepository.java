package com.example.aichat.category.infrastructure;

import com.example.aichat.category.domain.CategoryScope;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, Long> {

    List<CategoryJpaEntity> findByScopeAndActiveTrueOrderByDisplayOrderAscIdAsc(CategoryScope scope);

    Optional<CategoryJpaEntity> findByScopeAndSlugAndActiveTrue(CategoryScope scope, String slug);

    List<CategoryJpaEntity> findByIdIn(Collection<Long> ids);
}
