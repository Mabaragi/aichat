package com.example.aichat.category.application;

import com.example.aichat.category.domain.Category;
import com.example.aichat.category.domain.CategoryRepository;
import com.example.aichat.category.domain.CategoryScope;
import com.example.aichat.common.exception.BusinessException;
import com.example.aichat.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryResolver {

    private final CategoryRepository categoryRepository;

    public CategoryResolver(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category requireActive(CategoryScope scope, String slug) {
        String normalized = Category.normalizeSlug(slug);
        return categoryRepository.findActiveByScopeAndSlug(scope, normalized)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.CATEGORY_NOT_FOUND,
                        "Category not found: " + scope + "/" + normalized
                ));
    }

    public Map<Long, CategorySummaryView> summariesById(Collection<Long> ids) {
        return categoryRepository.findByIds(ids)
                .stream()
                .collect(Collectors.toMap(
                        Category::getId,
                        category -> CategorySummaryView.from(category),
                        (left, right) -> left
                ));
    }

    public CategorySummaryView summary(Category category) {
        return CategorySummaryView.from(category);
    }
}
