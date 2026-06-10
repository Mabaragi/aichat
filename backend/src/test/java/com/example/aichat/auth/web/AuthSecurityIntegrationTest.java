package com.example.aichat.auth.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("delete from refresh_tokens");
        jdbcTemplate.update("delete from characters");
        jdbcTemplate.update("delete from users");
    }

    @Test
    void signupTokenAuthenticatesProtectedEndpointsAndDerivesOwner() throws Exception {
        JsonNode signup = signup("owner@example.com", "owner");
        String accessToken = signup.get("accessToken").asText();
        long userId = signup.get("user").get("id").asLong();

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId));

        mockMvc.perform(post("/api/characters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content("""
                                {
                                  "name": "private character",
                                  "visibility": "PRIVATE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ownerId").value(userId));
    }

    @Test
    void protectedMutationWithoutBearerTokenReturnsCommonUnauthorizedResponse() throws Exception {
        mockMvc.perform(post("/api/characters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "character",
                                  "visibility": "PRIVATE"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void privateCharacterIsHiddenFromAnonymousAndOtherUser() throws Exception {
        JsonNode owner = signup("owner@example.com", "owner");
        String ownerToken = owner.get("accessToken").asText();
        long characterId = createPrivateCharacter(ownerToken);
        String otherToken = signup("other@example.com", "other")
                .get("accessToken").asText();

        mockMvc.perform(get("/api/characters/{id}", characterId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CHARACTER_NOT_FOUND"));

        mockMvc.perform(patch("/api/characters/{id}", characterId)
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "stolen"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CHARACTER_NOT_FOUND"));
    }

    @Test
    void refreshRotatesAndOldTokenReuseIsRejected() throws Exception {
        JsonNode signup = signup("owner@example.com", "owner");
        String oldRefresh = signup.get("refreshToken").asText();

        String firstRefreshBody = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("refreshToken", oldRefresh))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode rotated = objectMapper.readTree(firstRefreshBody);

        assertThat(rotated.get("refreshToken").asText()).isNotEqualTo(oldRefresh);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("refreshToken", oldRefresh))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("REFRESH_TOKEN_REUSED"));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "refreshToken", rotated.get("refreshToken").asText()
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("REFRESH_TOKEN_REUSED"));
    }

    @Test
    void loginReturnsSameUnauthorizedErrorForUnknownEmailAndWrongPassword() throws Exception {
        signup("owner@example.com", "owner");

        assertInvalidLogin("missing@example.com", "password123");
        assertInvalidLogin("owner@example.com", "wrong-password");
    }

    private JsonNode signup(String email, String nickname) throws Exception {
        String body = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "email", email,
                                "password", "password123",
                                "nickname", nickname
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(body);
    }

    private long createPrivateCharacter(String token) throws Exception {
        String body = mockMvc.perform(post("/api/characters")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "private character",
                                  "visibility": "PRIVATE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private void assertInvalidLogin(String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("Email or password is invalid"));
    }
}
