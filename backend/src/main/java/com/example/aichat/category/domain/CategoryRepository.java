package com.example.aichat.category.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

    List<Category> findActiveByScope(CategoryScope scope);

    Optional<Category> findActiveByScopeAndSlug(CategoryScope scope, String slug);

    List<Category> findByIds(Collection<Long> categoryIds);
}
