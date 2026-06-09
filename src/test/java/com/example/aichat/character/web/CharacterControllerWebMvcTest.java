package com.example.aichat.character.web;

import com.example.aichat.character.application.CharacterView;
import com.example.aichat.character.application.CreateCharacterCommand;
import com.example.aichat.character.application.CreateCharacterUseCase;
import com.example.aichat.character.application.DeleteCharacterUseCase;
import com.example.aichat.character.application.GetCharacterUseCase;
import com.example.aichat.character.application.ListCharactersResult;
import com.example.aichat.character.application.ListCharactersUseCase;
import com.example.aichat.character.application.UpdateCharacterCommand;
import com.example.aichat.character.application.UpdateCharacterUseCase;
import com.example.aichat.character.domain.Personality;
import com.example.aichat.character.domain.SpeechStyle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CharacterControllerWebMvcTest {

    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2026, 6, 8, 12, 0);

    @Mock
    private CreateCharacterUseCase createCharacterUseCase;

    @Mock
    private GetCharacterUseCase getCharacterUseCase;

    @Mock
    private ListCharactersUseCase listCharactersUseCase;

    @Mock
    private UpdateCharacterUseCase updateCharacterUseCase;

    @Mock
    private DeleteCharacterUseCase deleteCharacterUseCase;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().build();
        CharacterController controller = new CharacterController(
                createCharacterUseCase,
                getCharacterUseCase,
                listCharactersUseCase,
                updateCharacterUseCase,
                deleteCharacterUseCase,
                objectMapper
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new JacksonJsonHttpMessageConverter())
                .build();
    }

    @Test
    void createCharacterDelegatesToUseCaseAndReturnsCreatedResponse() throws Exception {
        when(createCharacterUseCase.execute(any())).thenReturn(sampleView(1L, "합리주의 미식가", "PRIVATE"));

        mockMvc.perform(post("/api/characters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ownerId": 1,
                                  "name": "합리주의 미식가",
                                  "description": "논리적이고 차분하게 음식 취향을 분석하는 캐릭터",
                                  "personality": {
                                    "rationality": 90,
                                    "humor": 30
                                  },
                                  "speechStyle": {
                                    "tone": "차분함",
                                    "formality": "높음"
                                  },
                                  "visibility": "PRIVATE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.ownerId").value(1))
                .andExpect(jsonPath("$.name").value("합리주의 미식가"))
                .andExpect(jsonPath("$.description").value("논리적이고 차분하게 음식 취향을 분석하는 캐릭터"))
                .andExpect(jsonPath("$.visibility").value("PRIVATE"))
                .andExpect(jsonPath("$.createdAt").value("2026-06-08T12:00:00"));

        ArgumentCaptor<CreateCharacterCommand> commandCaptor = ArgumentCaptor.forClass(CreateCharacterCommand.class);
        verify(createCharacterUseCase).execute(commandCaptor.capture());

        CreateCharacterCommand command = commandCaptor.getValue();
        assertThat(command.ownerId()).isEqualTo(1L);
        assertThat(command.name()).isEqualTo("합리주의 미식가");
        assertThat(command.description()).isEqualTo("논리적이고 차분하게 음식 취향을 분석하는 캐릭터");
        assertThat(command.personality()).isEqualTo(Personality.of("{\"rationality\":90,\"humor\":30}"));
        assertThat(command.speechStyle()).isEqualTo(SpeechStyle.of("{\"tone\":\"차분함\",\"formality\":\"높음\"}"));
        assertThat(command.visibility()).isEqualTo("PRIVATE");
    }

    @Test
    void getCharacterDelegatesToUseCaseAndReturnsResponse() throws Exception {
        when(getCharacterUseCase.execute(11L)).thenReturn(sampleView(11L, "조회 캐릭터", "PUBLIC"));

        mockMvc.perform(get("/api/characters/{characterId}", 11L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.ownerId").value(1))
                .andExpect(jsonPath("$.name").value("조회 캐릭터"))
                .andExpect(jsonPath("$.visibility").value("PUBLIC"))
                .andExpect(jsonPath("$.createdAt").value("2026-06-08T12:00:00"));

        verify(getCharacterUseCase).execute(11L);
    }

    @Test
    void listCharactersDelegatesToUseCaseAndReturnsArray() throws Exception {
        when(listCharactersUseCase.execute(1L)).thenReturn(new ListCharactersResult(List.of(
                sampleView(21L, "첫 번째", "PRIVATE"),
                sampleView(22L, "두 번째", "PUBLIC")
        )));

        mockMvc.perform(get("/api/characters")
                        .param("ownerId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(21))
                .andExpect(jsonPath("$[0].name").value("첫 번째"))
                .andExpect(jsonPath("$[1].id").value(22))
                .andExpect(jsonPath("$[1].name").value("두 번째"));

        verify(listCharactersUseCase).execute(1L);
    }

    @Test
    void updateCharacterDelegatesToUseCaseAndReturnsResponse() throws Exception {
        when(updateCharacterUseCase.execute(any())).thenReturn(sampleView(33L, "수정 캐릭터", "PUBLIC"));

        mockMvc.perform(patch("/api/characters/{characterId}", 33L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "수정 캐릭터",
                                  "personality": {
                                    "empathy": 80
                                  },
                                  "visibility": "PUBLIC"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(33))
                .andExpect(jsonPath("$.name").value("수정 캐릭터"))
                .andExpect(jsonPath("$.visibility").value("PUBLIC"))
                .andExpect(jsonPath("$.createdAt").value("2026-06-08T12:00:00"));

        ArgumentCaptor<UpdateCharacterCommand> commandCaptor = ArgumentCaptor.forClass(UpdateCharacterCommand.class);
        verify(updateCharacterUseCase).execute(commandCaptor.capture());

        UpdateCharacterCommand command = commandCaptor.getValue();
        assertThat(command.characterId()).isEqualTo(33L);
        assertThat(command.name()).isEqualTo("수정 캐릭터");
        assertThat(command.description()).isNull();
        assertThat(command.personality()).isEqualTo(Personality.of("{\"empathy\":80}"));
        assertThat(command.speechStyle()).isNull();
        assertThat(command.visibility()).isEqualTo("PUBLIC");
    }

    @Test
    void deleteCharacterDelegatesToUseCaseAndReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/characters/{characterId}", 44L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(deleteCharacterUseCase).execute(44L);
    }

    private static CharacterView sampleView(Long id, String name, String visibility) {
        return new CharacterView(
                id,
                1L,
                name,
                "논리적이고 차분하게 음식 취향을 분석하는 캐릭터",
                "{\"rationality\":90}",
                "{\"tone\":\"차분함\"}",
                visibility,
                FIXED_TIME,
                FIXED_TIME
        );
    }
}
