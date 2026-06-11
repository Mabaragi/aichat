package com.example.aichat.category.infrastructure;

import com.example.aichat.category.domain.Category;
import com.example.aichat.category.domain.CategoryRepository;
import com.example.aichat.category.domain.CategoryScope;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaCategoryRepositoryAdapter implements CategoryRepository {

    private final CategoryJpaRepository jpaRepository;

    public JpaCategoryRepositoryAdapter(CategoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Category> findActiveByScope(CategoryScope scope) {
        return jpaRepository.findByScopeAndActiveTrueOrderByDisplayOrderAscIdAsc(scope)
                .stream()
                .map(JpaCategoryRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    public Optional<Category> findActiveByScopeAndSlug(CategoryScope scope, String slug) {
        return jpaRepository.findByScopeAndSlugAndActiveTrue(scope, slug)
                .map(JpaCategoryRepositoryAdapter::toDomain);
    }

    @Override
    public List<Category> findByIds(Collection<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return List.of();
        }
        return jpaRepository.findByIdIn(categoryIds).stream()
                .map(JpaCategoryRepositoryAdapter::toDomain)
                .toList();
    }

    private static Category toDomain(CategoryJpaEntity entity) {
        return new Category(
                entity.id(),
                entity.scope(),
                entity.slug(),
                entity.name(),
                entity.description(),
                entity.displayOrder(),
                entity.active(),
                entity.createdAt(),
                entity.updatedAt()
        );
    }
}
