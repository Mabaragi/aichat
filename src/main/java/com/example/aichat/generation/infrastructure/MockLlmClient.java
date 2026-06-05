package com.example.aichat.generation.infrastructure;

import com.example.aichat.generation.domain.LlmClient;
import com.example.aichat.generation.domain.LlmGenerateCommand;
import com.example.aichat.generation.domain.LlmGenerateResult;

public class MockLlmClient implements LlmClient {

    @Override
    public LlmGenerateResult generate(LlmGenerateCommand command) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
