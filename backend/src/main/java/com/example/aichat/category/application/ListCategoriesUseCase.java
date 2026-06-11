package com.example.aichat.category.application;

import com.example.aichat.category.domain.CategoryRepository;
import com.example.aichat.category.domain.CategoryScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListCategoriesUseCase {

    private final CategoryRepository categoryRepository;

    public ListCategoriesUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategorySummaryView> execute(CategoryScope scope) {
        return categoryRepository.findActiveByScope(scope)
                .stream()
                .map(CategorySummaryView::from)
                .toList();
    }
}
