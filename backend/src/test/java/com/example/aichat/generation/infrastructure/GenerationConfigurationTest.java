package com.example.aichat.generation.infrastructure;

import com.example.aichat.generation.application.GenerationException;
import com.example.aichat.generation.application.TextGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class GenerationConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withUserConfiguration(GenerationConfiguration.class);

    @Test
    void resolvesMockTextGeneratorAsPrimaryWhenProviderIsMock() {
        contextRunner
                .withPropertyValues(
                        "generation.provider=mock",
                        "generation.mock.model-name=mock-model"
                )
                .run(context -> {
                    MockTextGenerator mockBean = context.getBean("mockTextGenerator", MockTextGenerator.class);
                    TextGenerator primaryBean = context.getBean(TextGenerator.class);

                    assertThat(primaryBean).isSameAs(mockBean);
                    assertThat(context.getBean("textGenerator", TextGenerator.class))
                            .isSameAs(mockBean);
                });
    }

    @Test
    void failsFastWhenOpenAiProviderHasNoApiKey() {
        contextRunner
                .withPropertyValues("generation.provider=openai")
                .run(context -> {
                    assertThat(context.getStartupFailure())
                            .isNotNull()
                            .hasRootCauseInstanceOf(GenerationException.class)
                            .hasRootCauseMessage("OPENAI_API_KEY is required");
                });
    }

    @Test
    void failsFastWhenGeminiProviderHasNoApiKey() {
        contextRunner
                .withPropertyValues("generation.provider=gemini")
                .run(context -> {
                    assertThat(context.getStartupFailure())
                            .isNotNull()
                            .hasRootCauseInstanceOf(GenerationException.class)
                            .hasRootCauseMessage("GEMINI_API_KEY is required");
                });
    }
}
