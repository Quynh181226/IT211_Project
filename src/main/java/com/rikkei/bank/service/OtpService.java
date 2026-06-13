package com.rikkei.bank.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class OtpService {

    private final Map<String, OtpData> otpStore = new ConcurrentHashMap<>();
    private static final long OTP_EXPIRY_MILLIS = 5 * 60 * 1000; // 5 phút

    public String generateOtp(String username) {
        String otp = String.format("%06d", (int)(Math.random() * 1000000));
        otpStore.put(username, new OtpData(otp, System.currentTimeMillis() + OTP_EXPIRY_MILLIS));
        log.info("Generated OTP for user: {}", username);
        return otp;
    }

    public boolean verifyOtp(String username, String otp) {
        OtpData data = otpStore.get(username);
        if (data == null) return false;
        if (System.currentTimeMillis() > data.expiryTime) {
            otpStore.remove(username);
            return false;
        }
        return data.otp.equals(otp);
    }

    public void removeOtp(String username) {
        otpStore.remove(username);
    }

    private static class OtpData {
        String otp;
        long expiryTime;
        OtpData(String otp, long expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }
    }
}