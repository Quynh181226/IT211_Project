package com.rikkei.bank.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:";

    public void blacklistToken(String token, long ttlSeconds) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "revoked", Duration.ofSeconds(ttlSeconds));
        log.info("Token blacklisted in Redis: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
    }

    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void blacklistTokenWithJwtExpiration(String token, long expirationMillis) {
        long ttlSeconds = Math.max(1, (expirationMillis - System.currentTimeMillis()) / 1000);
        blacklistToken(token, ttlSeconds);
    }
}