package com.example.aichat.character.application;

import com.example.aichat.character.infrastructure.InMemoryCharacterRepository;
import com.example.aichat.common.time.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

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
        createCharacterUseCase = new CreateCharacterUseCase(repository, timeProvider);
        getCharacterUseCase = new GetCharacterUseCase(repository);
        listCharactersUseCase = new ListCharactersUseCase(repository);
        updateCharacterUseCase = new UpdateCharacterUseCase(repository, timeProvider);
        deleteCharacterUseCase = new DeleteCharacterUseCase(repository);
    }

    @Test
    void createGetListUpdateDeleteCharacterFlowWorks() {
        CharacterView created = createCharacterUseCase.execute(new CreateCharacterCommand(
                1L,
                "합리주의 미식가",
                "논리적이고 차분하게 음식 취향을 분석하는 캐릭터",
                "{\"rationality\":90}",
                "{\"tone\":\"차분함\"}",
                null
        ));

        assertThat(created.id()).isEqualTo(1L);
        assertThat(created.visibility()).isEqualTo("PRIVATE");

        CharacterView fetched = getCharacterUseCase.execute(created.id());
        assertThat(fetched.name()).isEqualTo("합리주의 미식가");

        ListCharactersResult listed = listCharactersUseCase.execute(1L);
        assertThat(listed.items()).hasSize(1);

        CharacterView updated = updateCharacterUseCase.execute(new UpdateCharacterCommand(
                created.id(),
                "새 이름",
                null,
                "{\"rationality\":95}",
                null,
                "PUBLIC"
        ));

        assertThat(updated.name()).isEqualTo("새 이름");
        assertThat(updated.visibility()).isEqualTo("PUBLIC");

        DeleteCharacterResult deleted = deleteCharacterUseCase.execute(created.id());
        assertThat(deleted.characterId()).isEqualTo(created.id());

        assertThatThrownBy(() -> getCharacterUseCase.execute(created.id()))
                .isInstanceOf(com.example.aichat.common.exception.BusinessException.class);
    }
}
