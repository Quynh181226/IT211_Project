package com.rikkei.bank.service.token;

public interface IRedisBlacklistService {

    void blacklistToken(String token, long ttlSeconds);

    boolean isBlacklisted(String token);

    void blacklistTokenWithJwtExpiration(String token, long expirationMillis);
}