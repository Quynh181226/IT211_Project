package com.rikkei.bank.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendOtp(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Rikkei Bank - Password Reset OTP");
        message.setText("Your OTP to reset password is: " + otp + "\n\nThis OTP is valid for 5 minutes.\nIf you did not request this, please ignore this email.");
        mailSender.send(message);
        log.info("OTP email sent to {}", toEmail);
    }
}