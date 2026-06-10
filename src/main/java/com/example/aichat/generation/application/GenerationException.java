package com.example.aichat.generation.application;

public class GenerationException extends RuntimeException {

    private final String provider;
    private final String modelName;

    public GenerationException(String provider, String modelName, String message) {
        super(message);
        this.provider = provider;
        this.modelName = modelName;
    }

    public GenerationException(String provider, String modelName, String message, Throwable cause) {
        super(message, cause);
        this.provider = provider;
        this.modelName = modelName;
    }

    public String provider() {
        return provider;
    }

    public String modelName() {
        return modelName;
    }
}
