package com.rikkei.bank.service.auth;

import com.rikkei.bank.dto.auth.request.LoginRequest;
import com.rikkei.bank.dto.auth.request.RegisterRequest;
import com.rikkei.bank.dto.auth.response.LoginResponse;
import com.rikkei.bank.dto.auth.response.RefreshTokenResponse;
import com.rikkei.bank.entity.User;

public interface IAuthService {

    void register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    RefreshTokenResponse refreshToken(String refreshTokenString);

    void logout(String accessToken, Long userId);

    User getCurrentUser(String username);

    void forgotPassword(String username);

    void resetPassword(String username, String otp, String newPassword);
}