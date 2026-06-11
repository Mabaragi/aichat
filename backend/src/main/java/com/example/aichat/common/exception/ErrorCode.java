package com.example.aichat.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Machine-readable application error code.")
public enum ErrorCode {
    EMAIL_ALREADY_EXISTS,
    INVALID_CREDENTIALS,
    INVALID_TOKEN,
    REFRESH_TOKEN_REUSED,
    UNAUTHORIZED,
    USER_NOT_FOUND,
    CATEGORY_NOT_FOUND,
    CHARACTER_NOT_FOUND,
    DEBATE_SESSION_NOT_FOUND,
    DEBATE_PARTICIPANT_NOT_FOUND,
    INVALID_SESSION_STATE,
    INVALID_DEBATE_RULE,
    INVALID_PARTICIPANT_COUNT,
    DUPLICATED_SPEAKING_ORDER,
    TURN_GENERATION_FAILED,
    SHARED_CONTENT_NOT_FOUND
}
