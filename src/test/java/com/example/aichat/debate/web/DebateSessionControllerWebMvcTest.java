package com.example.aichat.debate.web;

import com.example.aichat.debate.application.CreateDebateSessionCommand;
import com.example.aichat.debate.application.CreateDebateSessionUseCase;
import com.example.aichat.debate.application.DebateParticipantView;
import com.example.aichat.debate.application.DebateSessionView;
import com.example.aichat.debate.domain.DebateFormat;
import com.example.aichat.debate.domain.DebateSessionStatus;
import com.example.aichat.debate.domain.ParticipantModel;
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
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DebateSessionControllerWebMvcTest {

    @Mock
    private CreateDebateSessionUseCase createDebateSessionUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = JsonMapper.builder().build();
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        DebateSessionController controller = new DebateSessionController(
                createDebateSessionUseCase,
                objectMapper
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new JacksonJsonHttpMessageConverter())
                .setValidator(validator)
                .build();
    }

    @Test
    void createSessionDelegatesCharacterSelectionsAndReturnsCreatedSnapshot() throws Exception {
        when(createDebateSessionUseCase.execute(any())).thenReturn(sampleView());

        mockMvc.perform(post("/api/debate-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ownerId": 1,
                                  "topic": {
                                    "title": "부먹 vs 찍먹",
                                    "description": "어느 방식이 더 나은가?",
                                    "category": "FOOD"
                                  },
                                  "format": "PROS_AND_CONS",
                                  "maxRounds": 5,
                                  "maxTurnLength": 600,
                                  "participants": [
                                    {"characterId": 10, "model": "FAST"},
                                    {"characterId": 10, "model": "QUALITY"}
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.participants.length()").value(2))
                .andExpect(jsonPath("$.participants[0].sourceCharacterId").value(10))
                .andExpect(jsonPath("$.participants[0].position").value(0))
                .andExpect(jsonPath("$.participants[0].name").value("합리주의 미식가"))
                .andExpect(jsonPath("$.participants[0].model").value("FAST"))
                .andExpect(jsonPath("$.participants[0].personality.rationality").value(90))
                .andExpect(jsonPath("$.participants[0].speechStyle.tone").value("차분함"))
                .andExpect(jsonPath("$.participants[1].sourceCharacterId").value(10))
                .andExpect(jsonPath("$.participants[1].position").value(1))
                .andExpect(jsonPath("$.participants[1].model").value("QUALITY"));

        ArgumentCaptor<CreateDebateSessionCommand> captor =
                ArgumentCaptor.forClass(CreateDebateSessionCommand.class);
        verify(createDebateSessionUseCase).execute(captor.capture());

        CreateDebateSessionCommand command = captor.getValue();
        assertThat(command.ownerId()).isEqualTo(1L);
        assertThat(command.topicTitle()).isEqualTo("부먹 vs 찍먹");
        assertThat(command.participants())
                .extracting(participant -> participant.characterId())
                .containsExactly(10L, 10L);
        assertThat(command.participants())
                .extracting(participant -> participant.model())
                .containsExactly(ParticipantModel.FAST, ParticipantModel.QUALITY);
    }

    @Test
    void rejectRequestWithoutExactlyTwoParticipants() throws Exception {
        mockMvc.perform(post("/api/debate-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ownerId": 1,
                                  "topic": {
                                    "title": "부먹 vs 찍먹",
                                    "description": "어느 방식이 더 나은가?",
                                    "category": "FOOD"
                                  },
                                  "format": "PROS_AND_CONS",
                                  "maxRounds": 5,
                                  "maxTurnLength": 600,
                                  "participants": [
                                    {"characterId": 10, "model": "FAST"}
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createDebateSessionUseCase);
    }

    private static DebateSessionView sampleView() {
        DebateParticipantView first = new DebateParticipantView(
                1L,
                10L,
                0,
                ParticipantModel.FAST,
                "합리주의 미식가",
                "논리적인 캐릭터",
                "{\"rationality\":90}",
                "{\"tone\":\"차분함\"}"
        );
        DebateParticipantView second = new DebateParticipantView(
                2L,
                10L,
                1,
                ParticipantModel.QUALITY,
                "합리주의 미식가",
                "논리적인 캐릭터",
                "{\"rationality\":90}",
                "{\"tone\":\"차분함\"}"
        );

        return new DebateSessionView(
                1L,
                1L,
                "부먹 vs 찍먹",
                "어느 방식이 더 나은가?",
                "FOOD",
                DebateSessionStatus.CREATED,
                DebateFormat.PROS_AND_CONS,
                5,
                0,
                600,
                List.of(first, second),
                LocalDateTime.of(2026, 6, 10, 12, 0)
        );
    }
}
