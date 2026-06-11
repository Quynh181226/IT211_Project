package com.rikkei.bank.service;

import com.rikkei.bank.entity.TokenBlacklist;
import com.rikkei.bank.repository.TokenBlacklistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlacklistService {

    private final TokenBlacklistRepository tokenBlacklistRepository;

    // Thêm token vào blacklist
    public void blacklistToken(String token) {
        // Token sẽ hết hạn sau 5 phút (tương ứng với thời gian sống của access token)
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(5);

        TokenBlacklist blacklist = TokenBlacklist.builder()
                .token(token)
                .expiryDate(expiryDate)
                .build();

        tokenBlacklistRepository.save(blacklist);
        log.info("Token blacklisted: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
    }

    // Kiểm tra token có trong blacklist không
    public boolean isBlacklisted(String token) {
        return tokenBlacklistRepository.existsByToken(token);
    }

    // Xóa token hết hạn (chạy mỗi giờ)
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        tokenBlacklistRepository.deleteAllExpiredTokens(now);
        log.info("Cleaned up expired blacklisted tokens at: {}", now);
    }
}