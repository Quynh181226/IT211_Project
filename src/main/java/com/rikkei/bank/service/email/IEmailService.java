package com.rikkei.bank.service.email;

public interface IEmailService {

    void sendOtp(String toEmail, String otp);
}