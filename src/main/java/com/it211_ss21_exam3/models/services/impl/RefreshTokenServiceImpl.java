package com.it211_ss21_exam3.models.services.impl;

import com.it211_ss21_exam3.exceptions.HttpBadRequestException;
import com.it211_ss21_exam3.models.entities.RefreshToken;
import com.it211_ss21_exam3.models.entities.User;
import com.it211_ss21_exam3.models.repositories.IRefreshTokenRepository;
import com.it211_ss21_exam3.models.repositories.IUserRepository;
import com.it211_ss21_exam3.models.services.IRefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements IRefreshTokenService {

    private final IRefreshTokenRepository refreshTokenRepository;
    private final IUserRepository userRepository;

    @Value("${jwt.expired.refresh}")
    private Long refreshTokenDurationMs;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new HttpBadRequestException("User not found with id: " + userId));

        refreshTokenRepository.revokeAllByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new HttpBadRequestException("Refresh token not found: " + token));
    }

    @Override
    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isRevoked()) {
            refreshTokenRepository.delete(token);
            throw new HttpBadRequestException("Refresh token was revoked. Please login again");
        }

        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new HttpBadRequestException("Refresh token expired. Please login again");
        }

        return token;
    }

    @Override
    @Transactional
    public void deleteAllByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new HttpBadRequestException("User not found"));
        refreshTokenRepository.deleteAllByUser(user);
        log.info("Deleted all refresh tokens for user ID: {}", userId);
    }

    @Override
    @Transactional
    public void revokeAllByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new HttpBadRequestException("User not found"));
        refreshTokenRepository.revokeAllByUser(user);
        log.info("Revoked all refresh tokens for user ID: {}", userId);
    }
}