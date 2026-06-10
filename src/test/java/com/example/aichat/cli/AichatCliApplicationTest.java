package com.example.aichat.cli;

import com.example.aichat.debate.application.GenerateNextTurnUseCase;
import com.example.aichat.user.application.CreateUserUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

class AichatCliApplicationTest {

    @Test
    void startsWithoutWebServerUsingTestDatabaseProfile() throws Exception {
        String[] args = {"--spring.profiles.active=test"};

        try (ConfigurableApplicationContext context = AichatCliApplication.startContext(args)) {
            assertThat(context.getBean(CliRunner.class)).isNotNull();
            assertThat(context.getBean(CreateUserUseCase.class)).isNotNull();
            assertThat(context.getBean(GenerateNextTurnUseCase.class)).isNotNull();
            assertThat(context.getBean(PasswordEncoder.class)).isNotNull();
            assertThat(context.getClass().getName()).doesNotContain("ServletWebServerApplicationContext");
            assertThat(context.getEnvironment().getActiveProfiles()).contains("test");

            try (Connection connection = context.getBean(DataSource.class).getConnection()) {
                assertThat(connection.getMetaData().getURL())
                        .contains("target/aichat-test.db");
            }
        }
    }
}
