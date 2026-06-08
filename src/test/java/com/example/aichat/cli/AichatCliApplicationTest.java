package com.example.aichat.cli;

import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

class AichatCliApplicationTest {

    @Test
    void startsWithoutWebServer() {
        try (ConfigurableApplicationContext context = AichatCliApplication.startContext(new String[0])) {
            assertThat(context.getBean(CliRunner.class)).isNotNull();
            assertThat(context.getClass().getName()).doesNotContain("ServletWebServerApplicationContext");
        }
    }
}
