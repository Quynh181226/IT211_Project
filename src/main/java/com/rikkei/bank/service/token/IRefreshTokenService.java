package com.rikkei.bank.service.token;

import com.rikkei.bank.entity.RefreshToken;

public interface IRefreshTokenService {

    RefreshToken createRefreshToken(Long userId);

    RefreshToken findByToken(String token);

    RefreshToken verifyExpiration(RefreshToken token);

    void revokeAllByUser(Long userId);
}