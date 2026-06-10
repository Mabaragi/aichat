package com.example.aichat.auth.application;

import com.example.aichat.auth.domain.RefreshToken;
import com.example.aichat.auth.domain.RefreshTokenRepository;
import com.example.aichat.common.exception.BusinessException;
import com.example.aichat.common.exception.ErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class RefreshTokenRotationService {

    private final RefreshTokenRepository repository;

    public RefreshTokenRotationService(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public synchronized RotationResult rotate(String currentHash, RefreshToken replacement,
                                              LocalDateTime now) {
        RefreshToken current = repository.findByTokenHash(currentHash)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_TOKEN,
                        "Token is invalid or expired"
                ));
        if (!current.isActive(now)) {
            repository.revokeFamily(current.getFamilyId(), now);
            return RotationResult.REUSED;
        }

        current.rotate(replacement.getTokenHash(), now);
        repository.save(current);
        repository.save(replacement);
        return RotationResult.SUCCESS;
    }

    public enum RotationResult {
        SUCCESS,
        REUSED
    }
}
