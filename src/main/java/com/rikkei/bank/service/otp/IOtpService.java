package com.rikkei.bank.service.otp;

public interface IOtpService {

    String generateOtp(String username);

    boolean verifyOtp(String username, String otp);

    void removeOtp(String username);
}