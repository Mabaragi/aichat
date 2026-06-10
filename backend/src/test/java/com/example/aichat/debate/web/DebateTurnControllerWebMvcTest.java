package com.example.aichat.debate.web;

import com.example.aichat.debate.application.DebateTurnView;
import com.example.aichat.debate.application.GenerateDebateTurnCommand;
import com.example.aichat.debate.application.GenerateDebateTurnUseCase;
import com.example.aichat.debate.application.ListDebateTurnsCommand;
import com.example.aichat.debate.application.ListDebateTurnsUseCase;
import com.example.aichat.debate.domain.ParticipantModel;
import com.example.aichat.debate.domain.TurnStatus;
import com.example.aichat.debate.domain.TurnType;
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
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DebateTurnControllerWebMvcTest {

    @Mock
    private GenerateDebateTurnUseCase generateDebateTurnUseCase;

    @Mock
    private ListDebateTurnsUseCase listDebateTurnsUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = JsonMapper.builder().build();
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        DebateTurnController controller = new DebateTurnController(
                generateDebateTurnUseCase,
                listDebateTurnsUseCase
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new JacksonJsonHttpMessageConverter())
                .setValidator(validator)
                .build();
    }

    @Test
    void generateTurnDelegatesAndReturnsCreatedTurn() throws Exception {
        when(generateDebateTurnUseCase.execute(any())).thenReturn(sampleGeneratedTurn());

        mockMvc.perform(post("/api/debate-sessions/1/turns/generate")
                        .principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sessionId").value(1))
                .andExpect(jsonPath("$.participantId").value(10))
                .andExpect(jsonPath("$.turnIndex").value(1))
                .andExpect(jsonPath("$.type").value("ARGUMENT"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.content").value("부먹은 소스와 튀김의 조화를 극대화합니다."))
                .andExpect(jsonPath("$.modelName").value("mock-model"));

        ArgumentCaptor<GenerateDebateTurnCommand> captor =
                ArgumentCaptor.forClass(GenerateDebateTurnCommand.class);
        verify(generateDebateTurnUseCase).execute(captor.capture());
        assertThat(captor.getValue().sessionId()).isEqualTo(1L);
        assertThat(captor.getValue().actor().userId()).isEqualTo(1L);
    }

    @Test
    void listTurnsDelegatesAndReturnsSummariesInOrder() throws Exception {
        when(listDebateTurnsUseCase.execute(any())).thenReturn(List.of(
                new DebateTurnView(
                        1L,
                        1L,
                        10L,
                        ParticipantModel.FAST,
                        1,
                        1,
                        TurnType.ARGUMENT,
                        TurnStatus.COMPLETED,
                        "첫 번째 발화",
                        "prompt-1",
                        "mock-model",
                        0,
                        0,
                        LocalDateTime.of(2026, 6, 10, 12, 2)
                ),
                new DebateTurnView(
                        2L,
                        1L,
                        20L,
                        ParticipantModel.QUALITY,
                        1,
                        2,
                        TurnType.REBUTTAL,
                        TurnStatus.COMPLETED,
                        "두 번째 발화",
                        "prompt-2",
                        "mock-model",
                        0,
                        0,
                        LocalDateTime.of(2026, 6, 10, 12, 3)
                )
        ));

        mockMvc.perform(get("/api/debate-sessions/1/turns")
                        .principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].participantId").value(10))
                .andExpect(jsonPath("$[0].participantModel").value("FAST"))
                .andExpect(jsonPath("$[0].turnIndex").value(1))
                .andExpect(jsonPath("$[1].participantId").value(20))
                .andExpect(jsonPath("$[1].participantModel").value("QUALITY"))
                .andExpect(jsonPath("$[1].turnIndex").value(2));

        ArgumentCaptor<ListDebateTurnsCommand> captor =
                ArgumentCaptor.forClass(ListDebateTurnsCommand.class);
        verify(listDebateTurnsUseCase).execute(captor.capture());
        assertThat(captor.getValue().sessionId()).isEqualTo(1L);
        assertThat(captor.getValue().actor().userId()).isEqualTo(1L);
    }

    private static DebateTurnView sampleGeneratedTurn() {
        return new DebateTurnView(
                1L,
                1L,
                10L,
                ParticipantModel.FAST,
                1,
                1,
                TurnType.ARGUMENT,
                TurnStatus.COMPLETED,
                "부먹은 소스와 튀김의 조화를 극대화합니다.",
                "prompt snapshot",
                "mock-model",
                0,
                0,
                LocalDateTime.of(2026, 6, 10, 12, 2)
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
