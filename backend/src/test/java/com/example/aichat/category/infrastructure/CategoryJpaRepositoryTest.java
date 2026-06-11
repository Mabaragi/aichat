package com.example.aichat.category.infrastructure;

import com.example.aichat.category.domain.CategoryScope;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CategoryJpaRepositoryTest {

    @Autowired
    private CategoryJpaRepository repository;

    @Test
    void flywaySeedsActiveDebateAndCharacterCategories() {
        assertThat(repository.findByScopeAndActiveTrueOrderByDisplayOrderAscIdAsc(CategoryScope.DEBATE))
                .extracting(CategoryJpaEntity::slug)
                .containsExactly("food", "culture", "tech", "life", "society", "fun", "other");

        assertThat(repository.findByScopeAndActiveTrueOrderByDisplayOrderAscIdAsc(CategoryScope.CHARACTER))
                .extracting(CategoryJpaEntity::slug)
                .containsExactly("expert", "critic", "creator", "storyteller", "comedy", "utility", "other");
    }
}
