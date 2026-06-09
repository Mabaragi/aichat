package com.example.aichat.user.web;

import com.example.aichat.user.application.CreateUserCommand;
import com.example.aichat.user.application.CreateUserUseCase;
import com.example.aichat.user.application.UserView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerWebMvcTest {

    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2026, 6, 5, 12, 0);

    @Mock
    private CreateUserUseCase createUserUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        UserController controller = new UserController(createUserUseCase);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new JacksonJsonHttpMessageConverter())
                .setValidator(validator)
                .build();
    }

    @Test
    void createUserDelegatesToUseCaseAndReturnsCreatedResponse() throws Exception {
        when(createUserUseCase.execute(any())).thenReturn(sampleView(1L, "user@example.com", "마바라기"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "nickname": "마바라기"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.nickname").value("마바라기"))
                .andExpect(jsonPath("$.createdAt").value("2026-06-05T12:00:00"));

        ArgumentCaptor<CreateUserCommand> commandCaptor = ArgumentCaptor.forClass(CreateUserCommand.class);
        verify(createUserUseCase).execute(commandCaptor.capture());

        CreateUserCommand command = commandCaptor.getValue();
        assertThat(command.email()).isEqualTo("user@example.com");
        assertThat(command.nickname()).isEqualTo("마바라기");
    }

    @Test
    void createUserRejectsInvalidPayloadBeforeUseCaseCall() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "   ",
                                  "nickname": "마바라기"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createUserUseCase);
    }

    private static UserView sampleView(Long id, String email, String nickname) {
        return new UserView(id, email, nickname, FIXED_TIME);
    }
}
