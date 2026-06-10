package com.example.aichat.user.web;

import com.example.aichat.user.application.GetUserUseCase;
import com.example.aichat.user.application.UserView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerWebMvcTest {

    @Mock
    private GetUserUseCase getUserUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(getUserUseCase))
                .setMessageConverters(new JacksonJsonHttpMessageConverter())
                .build();
    }

    @Test
    void returnsCurrentJwtUser() throws Exception {
        when(getUserUseCase.execute(1L)).thenReturn(new UserView(
                1L,
                "user@example.com",
                "마바라기",
                LocalDateTime.of(2026, 6, 10, 12, 0)
        ));

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("1")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        mockMvc.perform(get("/api/users/me")
                        .principal(new JwtAuthenticationToken(jwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }
}
