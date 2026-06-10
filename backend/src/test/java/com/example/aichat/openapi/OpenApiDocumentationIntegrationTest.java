package com.example.aichat.openapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiDocumentationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void exposesImplementedApiDocumentationWithBearerSecurityAndSchemas() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("AI Debate Platform API"))
                .andExpect(jsonPath("$.info.version").value("v1"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.paths['/api/auth/signup'].post").exists())
                .andExpect(jsonPath("$.paths['/api/auth/login'].post").exists())
                .andExpect(jsonPath("$.paths['/api/auth/refresh'].post").exists())
                .andExpect(jsonPath("$.paths['/api/auth/logout'].post").exists())
                .andExpect(jsonPath("$.paths['/api/users/me'].get.security[0].bearerAuth").isArray())
                .andExpect(jsonPath("$.paths['/api/characters'].post.security[0].bearerAuth").isArray())
                .andExpect(jsonPath("$.paths['/api/characters'].get.security").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/characters/{characterId}'].get.security").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/debate-sessions'].post.security[0].bearerAuth")
                        .isArray())
                .andExpect(jsonPath("$.paths['/api/debate-sessions/{sessionId}/start'].post").exists())
                .andExpect(jsonPath("$.paths['/api/debate-sessions/{sessionId}/complete'].post").exists())
                .andExpect(jsonPath("$.paths['/api/debate-sessions/{sessionId}/turns'].get").exists())
                .andExpect(jsonPath("$.paths['/api/debate-sessions/{sessionId}/turns/generate'].post")
                        .exists())
                .andExpect(jsonPath("$.paths['/api/shared-contents/{slug}']").doesNotExist())
                .andExpect(jsonPath("$.components.schemas.ErrorResponse").exists())
                .andExpect(jsonPath("$.components.schemas.AuthTokensResponse").exists())
                .andExpect(jsonPath("$.components.schemas.CreateCharacterRequest").exists())
                .andExpect(jsonPath("$.components.schemas.CreateDebateSessionRequest").exists())
                .andExpect(jsonPath("$.components.schemas.DebateSessionResponse").exists())
                .andExpect(jsonPath("$.components.schemas.StartDebateSessionResponse").exists())
                .andExpect(jsonPath("$.components.schemas.CompleteDebateSessionResponse").exists())
                .andExpect(jsonPath("$.components.schemas.CreateCharacterRequest"
                        + ".properties.personality.type").value("object"))
                .andExpect(jsonPath("$.components.schemas.CreateCharacterRequest"
                        + ".properties.speechStyle.type").value("object"))
                .andExpect(jsonPath("$.components.schemas.CharacterResponse"
                        + ".properties.personality.type").value("object"))
                .andExpect(jsonPath("$.components.schemas.CharacterResponse"
                        + ".properties.speechStyle.type").value("object"))
                .andExpect(jsonPath("$.components.schemas.DebateSessionParticipantResponse"
                        + ".properties.personality.type").value("object"))
                .andExpect(jsonPath("$.components.schemas.DebateSessionParticipantResponse"
                        + ".properties.speechStyle.type").value("object"))
                .andExpect(jsonPath("$.components.schemas.DebateTurnResponse").exists())
                .andExpect(jsonPath("$.components.schemas.GenerateTurnResponse").exists())
                .andExpect(jsonPath("$.components.schemas.SharedContentResponse").doesNotExist());
    }
}
