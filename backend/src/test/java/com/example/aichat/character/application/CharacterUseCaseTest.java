package com.example.aichat.character.application;

import com.example.aichat.category.application.CategoryResolver;
import com.example.aichat.category.domain.Category;
import com.example.aichat.category.domain.CategoryRepository;
import com.example.aichat.category.domain.CategoryScope;
import com.example.aichat.character.domain.Personality;
import com.example.aichat.character.domain.SpeechStyle;
import com.example.aichat.character.infrastructure.InMemoryCharacterRepository;
import com.example.aichat.common.time.TimeProvider;
import com.example.aichat.common.security.RequestActor;
import com.example.aichat.common.exception.BusinessException;
import com.example.aichat.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CharacterUseCaseTest {

    private InMemoryCharacterRepository repository;
    private TimeProvider timeProvider;
    private CreateCharacterUseCase createCharacterUseCase;
    private GetCharacterUseCase getCharacterUseCase;
    private ListCharactersUseCase listCharactersUseCase;
    private UpdateCharacterUseCase updateCharacterUseCase;
    private DeleteCharacterUseCase deleteCharacterUseCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryCharacterRepository();
        timeProvider = () -> LocalDateTime.of(2026, 6, 8, 12, 0);
        CategoryResolver categoryResolver = categoryResolver();
        createCharacterUseCase = new CreateCharacterUseCase(repository, categoryResolver, timeProvider);
        getCharacterUseCase = new GetCharacterUseCase(repository, categoryResolver);
        listCharactersUseCase = new ListCharactersUseCase(repository, categoryResolver);
        updateCharacterUseCase = new UpdateCharacterUseCase(repository, categoryResolver, timeProvider);
        deleteCharacterUseCase = new DeleteCharacterUseCase(repository);
    }

    @Test
    void createGetListUpdateDeleteCharacterFlowWorks() {
        CharacterView created = createCharacterUseCase.execute(new CreateCharacterCommand(
                1L,
                "합리주의 미식가",
                "논리적이고 차분하게 음식 취향을 분석하는 캐릭터",
                Personality.of("{\"rationality\":90}"),
                SpeechStyle.of("{\"tone\":\"차분함\"}"),
                null
        ));

        assertThat(created.id()).isEqualTo(1L);
        assertThat(created.visibility()).isEqualTo("PRIVATE");
        assertThat(created.personality()).isEqualTo("{\"rationality\":90}");
        assertThat(created.speechStyle()).isEqualTo("{\"tone\":\"차분함\"}");

        CharacterView fetched = getCharacterUseCase.execute(created.id());
        assertThat(fetched.name()).isEqualTo("합리주의 미식가");

        ListCharactersResult listed = listCharactersUseCase.execute(1L);
        assertThat(listed.items()).hasSize(1);

        CharacterView updated = updateCharacterUseCase.execute(new UpdateCharacterCommand(
                created.id(),
                "새 이름",
                null,
                Personality.of("{\"rationality\":95}"),
                null,
                "PUBLIC"
        ));

        assertThat(updated.name()).isEqualTo("새 이름");
        assertThat(updated.visibility()).isEqualTo("PUBLIC");
        assertThat(updated.personality()).isEqualTo("{\"rationality\":95}");
        assertThat(updated.speechStyle()).isEqualTo("{\"tone\":\"차분함\"}");

        DeleteCharacterResult deleted = deleteCharacterUseCase.execute(created.id());
        assertThat(deleted.characterId()).isEqualTo(created.id());

        assertThatThrownBy(() -> getCharacterUseCase.execute(created.id()))
                .isInstanceOf(com.example.aichat.common.exception.BusinessException.class);
    }

    @Test
    void anonymousCanReadOnlyPublicCharacters() {
        CharacterView privateCharacter = create("private", "PRIVATE");
        CharacterView publicCharacter = create("public", "PUBLIC");

        assertThatThrownBy(() -> getCharacterUseCase.execute(
                RequestActor.anonymous(), privateCharacter.id()))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.CHARACTER_NOT_FOUND));

        assertThat(getCharacterUseCase.execute(
                RequestActor.anonymous(), publicCharacter.id()).id())
                .isEqualTo(publicCharacter.id());
        assertThat(listCharactersUseCase.execute(RequestActor.anonymous(), 1L).items())
                .extracting(CharacterView::id)
                .containsExactly(publicCharacter.id());
    }

    @Test
    void otherUserCannotReadUpdateOrDeletePrivateCharacter() {
        CharacterView privateCharacter = create("private", "PRIVATE");
        RequestActor otherUser = RequestActor.authenticated(2L);

        assertNotFound(() -> getCharacterUseCase.execute(otherUser, privateCharacter.id()));
        assertNotFound(() -> updateCharacterUseCase.execute(new UpdateCharacterCommand(
                otherUser,
                privateCharacter.id(),
                "changed",
                null,
                null,
                null,
                null
        )));
        assertNotFound(() -> deleteCharacterUseCase.execute(otherUser, privateCharacter.id()));
    }

    private CharacterView create(String name, String visibility) {
        return createCharacterUseCase.execute(new CreateCharacterCommand(
                1L,
                name,
                null,
                null,
                null,
                visibility
        ));
    }

    private static void assertNotFound(
            org.assertj.core.api.ThrowableAssert.ThrowingCallable call) {
        assertThatThrownBy(call)
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.CHARACTER_NOT_FOUND));
    }

    private static CategoryResolver categoryResolver() {
        Category other = new Category(
                100L,
                CategoryScope.CHARACTER,
                "other",
                "기타",
                null,
                999,
                true,
                LocalDateTime.of(2026, 6, 8, 12, 0),
                LocalDateTime.of(2026, 6, 8, 12, 0)
        );
        return new CategoryResolver(new CategoryRepository() {
            @Override
            public List<Category> findActiveByScope(CategoryScope scope) {
                return scope == CategoryScope.CHARACTER ? List.of(other) : List.of();
            }

            @Override
            public Optional<Category> findActiveByScopeAndSlug(CategoryScope scope, String slug) {
                return scope == CategoryScope.CHARACTER && "other".equals(slug)
                        ? Optional.of(other)
                        : Optional.empty();
            }

            @Override
            public List<Category> findByIds(Collection<Long> categoryIds) {
                return categoryIds.contains(other.getId()) ? List.of(other) : List.of();
            }
        });
    }
}
