package com.example.aichat.debate.infrastructure;

import com.example.aichat.debate.domain.DebateTurnPromptBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DebateConfiguration {

    @Bean
    DebateTurnPromptBuilder debateTurnPromptBuilder() {
        return new DebateTurnPromptBuilder();
    }
}
