package com.it211_ss21_exam3.models.services;

import com.it211_ss21_exam3.models.entities.RefreshToken;

public interface IRefreshTokenService {

    RefreshToken createRefreshToken(Long userId);

    RefreshToken findByToken(String token);

    RefreshToken verifyExpiration(RefreshToken token);

    void deleteAllByUser(Long userId);

    void revokeAllByUser(Long userId);
}