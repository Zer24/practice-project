package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.RefreshToken;
import org.example.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.security.jwt.refresh-expiration:604800000}")
    private long refreshExpiration;

    @Transactional
    public void saveRefreshToken(UUID userId, String token) {
        refreshTokenRepository.deleteByUserId(userId);

        LocalDateTime expiryDate = LocalDateTime.now().plusNanos(refreshExpiration * 1_000_000);
        RefreshToken refreshToken = new RefreshToken(userId, token, expiryDate);
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (refreshToken.isRevoked()) {
            throw new RuntimeException("Refresh token has been revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token has expired");
        }

        return refreshToken;
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElse(null);
        if (refreshToken != null) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        }
    }

    @Transactional
    public void revokeAllUserTokens(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}