package com.example.aichat.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(name = "ErrorResponse", description = "Common error response payload.")
public record ErrorResponse(
        @Schema(description = "Application-specific error code.", example = "UNAUTHORIZED")
        ErrorCode code,
        @Schema(description = "Human-readable error message.",
                example = "Authentication is required")
        String message,
        @Schema(description = "Timestamp when the error response was generated.",
                example = "2026-06-10T12:00:00")
        LocalDateTime timestamp
) {
}
