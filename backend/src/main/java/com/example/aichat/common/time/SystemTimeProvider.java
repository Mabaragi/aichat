package com.example.aichat.common.time;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SystemTimeProvider implements TimeProvider {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
