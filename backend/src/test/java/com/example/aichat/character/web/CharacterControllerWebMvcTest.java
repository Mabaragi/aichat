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
import com.example.aichat.support.PersonaFixtures;
import com.example.aichat.common.security.RequestActor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.time.Instant;
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
                        .principal(authentication())
                        .content("""
                                {
                                  "name": "합리주의 미식가",
                                  "description": "논리적이고 차분하게 음식 취향을 분석하는 캐릭터",
                                  "persona": {
                                    "identity": "합리주의 미식가",
                                    "debateRole": "현실성 검증자",
                                    "coreValues": ["실증성", "논리"],
                                    "expertise": ["음식 문화"],
                                    "defaultStance": "취향보다 실행 조건과 경험 품질을 먼저 본다.",
                                    "evidenceStyle": "비교 사례와 비용-편익 분석을 선호한다.",
                                    "debateBehavior": ["상대 주장의 숨은 전제를 찾는다."],
                                    "voiceStyle": {
                                      "tone": "차분함",
                                      "sentenceLength": "중간",
                                      "rhetoricalStyle": "질문과 구조적 반박 중심",
                                      "signaturePhrases": ["핵심은 실행 조건입니다."]
                                    },
                                    "boundaries": {
                                      "mustDo": ["상대 주장을 먼저 요약한다.", "불확실한 사실은 단정하지 않는다."],
                                      "mustNotDo": ["인신공격하지 않는다.", "출처 없는 수치를 만들지 않는다."]
                                    },
                                    "exampleLines": ["그 주장의 선의는 이해하지만, 실행 조건을 봐야 합니다."]
                                  },
                                  "visibility": "PRIVATE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.ownerId").value(1))
                .andExpect(jsonPath("$.name").value("합리주의 미식가"))
                .andExpect(jsonPath("$.description").value("논리적이고 차분하게 음식 취향을 분석하는 캐릭터"))
                .andExpect(jsonPath("$.persona.identity").value("합리주의 미식가"))
                .andExpect(jsonPath("$.personality").doesNotExist())
                .andExpect(jsonPath("$.speechStyle").doesNotExist())
                .andExpect(jsonPath("$.visibility").value("PRIVATE"))
                .andExpect(jsonPath("$.createdAt").value("2026-06-08T12:00:00"));

        ArgumentCaptor<CreateCharacterCommand> commandCaptor = ArgumentCaptor.forClass(CreateCharacterCommand.class);
        verify(createCharacterUseCase).execute(commandCaptor.capture());

        CreateCharacterCommand command = commandCaptor.getValue();
        assertThat(command.ownerId()).isEqualTo(1L);
        assertThat(command.actor()).isEqualTo(RequestActor.authenticated(1L));
        assertThat(command.name()).isEqualTo("합리주의 미식가");
        assertThat(command.description()).isEqualTo("논리적이고 차분하게 음식 취향을 분석하는 캐릭터");
        assertThat(command.persona()).isEqualTo(PersonaFixtures.rationalGourmet());
        assertThat(command.visibility()).isEqualTo("PRIVATE");
    }

    @Test
    void getCharacterDelegatesToUseCaseAndReturnsResponse() throws Exception {
        when(getCharacterUseCase.execute(any(RequestActor.class), org.mockito.ArgumentMatchers.eq(11L)))
                .thenReturn(sampleView(11L, "조회 캐릭터", "PUBLIC"));

        mockMvc.perform(get("/api/characters/{characterId}", 11L)
                        .principal(authentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.ownerId").value(1))
                .andExpect(jsonPath("$.name").value("조회 캐릭터"))
                .andExpect(jsonPath("$.visibility").value("PUBLIC"))
                .andExpect(jsonPath("$.createdAt").value("2026-06-08T12:00:00"));

        verify(getCharacterUseCase).execute(RequestActor.authenticated(1L), 11L);
    }

    @Test
    void listCharactersDelegatesToUseCaseAndReturnsArray() throws Exception {
        when(listCharactersUseCase.execute(any(RequestActor.class), org.mockito.ArgumentMatchers.eq(1L)))
                .thenReturn(new ListCharactersResult(List.of(
                sampleView(21L, "첫 번째", "PRIVATE"),
                sampleView(22L, "두 번째", "PUBLIC")
        )));

        mockMvc.perform(get("/api/characters")
                        .principal(authentication())
                        .param("ownerId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(21))
                .andExpect(jsonPath("$[0].name").value("첫 번째"))
                .andExpect(jsonPath("$[1].id").value(22))
                .andExpect(jsonPath("$[1].name").value("두 번째"));

        verify(listCharactersUseCase).execute(RequestActor.authenticated(1L), 1L);
    }

    @Test
    void updateCharacterDelegatesToUseCaseAndReturnsResponse() throws Exception {
        when(updateCharacterUseCase.execute(any())).thenReturn(sampleView(33L, "수정 캐릭터", "PUBLIC"));

        mockMvc.perform(patch("/api/characters/{characterId}", 33L)
                        .principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "수정 캐릭터",
                                  "persona": {
                                    "identity": "공감형 중재자",
                                    "debateRole": "중재자",
                                    "coreValues": ["공정성", "상호 이해"],
                                    "expertise": ["갈등 조정"],
                                    "defaultStance": "양쪽 주장의 강점을 먼저 확인한다.",
                                    "evidenceStyle": "균형 잡힌 사례와 원칙을 함께 본다.",
                                    "debateBehavior": ["공통분모를 찾는다."],
                                    "voiceStyle": {
                                      "tone": "친근함",
                                      "sentenceLength": "중간",
                                      "rhetoricalStyle": "요약과 조율 중심",
                                      "signaturePhrases": []
                                    },
                                    "boundaries": {
                                      "mustDo": ["상대 주장을 먼저 요약한다."],
                                      "mustNotDo": ["상대 주장을 왜곡하지 않는다."]
                                    },
                                    "exampleLines": []
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
        assertThat(command.actor()).isEqualTo(RequestActor.authenticated(1L));
        assertThat(command.name()).isEqualTo("수정 캐릭터");
        assertThat(command.description()).isNull();
        assertThat(command.persona()).isEqualTo(PersonaFixtures.empathetic());
        assertThat(command.visibility()).isEqualTo("PUBLIC");
    }

    @Test
    void deleteCharacterDelegatesToUseCaseAndReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/characters/{characterId}", 44L)
                        .principal(authentication()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(deleteCharacterUseCase).execute(RequestActor.authenticated(1L), 44L);
    }

    private static CharacterView sampleView(Long id, String name, String visibility) {
        return new CharacterView(
                id,
                1L,
                name,
                "논리적이고 차분하게 음식 취향을 분석하는 캐릭터",
                PersonaFixtures.rationalGourmetJson(),
                visibility,
                FIXED_TIME,
                FIXED_TIME
        );
    }

    private static JwtAuthenticationToken authentication() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("1")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();
        return new JwtAuthenticationToken(jwt);
    }
}
