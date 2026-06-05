package com.example.aichat.generation.domain;

public interface LlmClient {

    LlmGenerateResult generate(LlmGenerateCommand command);
}
