package com.example.aichat.generation.infrastructure;

import com.example.aichat.generation.application.GenerationException;
import com.example.aichat.generation.application.TextGenerator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableConfigurationProperties(GenerationProperties.class)
public class GenerationConfiguration {

    @Bean
    public MockTextGenerator mockTextGenerator() {
        return new MockTextGenerator();
    }

    @Bean
    @Primary
    public TextGenerator textGenerator(GenerationProperties properties, MockTextGenerator mockTextGenerator) {
        GenerationProperties.Provider provider = properties.provider() == null
                ? GenerationProperties.Provider.MOCK
                : properties.provider();

        return switch (provider) {
            case MOCK -> mockTextGenerator;
            case OPENAI -> new OpenAiTextGenerator(
                    OpenAiTextGenerator.createClient(properties.openaiApiKey())
            );
            case GEMINI -> new GeminiTextGenerator(
                    GeminiTextGenerator.createClient(properties.geminiApiKey())
            );
        };
    }
}
