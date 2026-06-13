package com.rikkei.bank.controller;

import com.rikkei.bank.annotation.LogExecutionTime;
import com.rikkei.bank.dto.request.*;
import com.rikkei.bank.dto.response.LoginResponse;
import com.rikkei.bank.dto.response.RefreshTokenResponse;
import com.rikkei.bank.dto.response.StandardResponse;
import com.rikkei.bank.entity.User;
import com.rikkei.bank.security.UserDetailsImpl;
import com.rikkei.bank.service.AuthService;
import com.rikkei.bank.service.UserService;
import com.rikkei.bank.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<StandardResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseUtil.created(null, "User registered successfully");
    }

    @LogExecutionTime
    @PostMapping("/login")
    public ResponseEntity<StandardResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseUtil.success(response, "Login successful");
    }

    @LogExecutionTime
    @PostMapping("/refresh")
    public ResponseEntity<StandardResponse<RefreshTokenResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseUtil.success(response, "Token refreshed successfully");
    }

    @PostMapping("/logout")
    public ResponseEntity<StandardResponse<Void>> logout(@RequestHeader("Authorization") String authorization,
                                                         @AuthenticationPrincipal UserDetailsImpl currentUser) {
        if (currentUser == null) {
            return ResponseUtil.error("User not authenticated", HttpStatus.UNAUTHORIZED);
        }

        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            authService.logout(token, currentUser.getId());
        }

        return ResponseUtil.success(null, "Logout successful");
    }

    @PutMapping("/change-pin")
    public ResponseEntity<StandardResponse<Void>> changePin(@Valid @RequestBody ChangePinRequest request,
                                                            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        User user = authService.getCurrentUser(currentUser.getUsername());
        userService.changePin(user, request);
        return ResponseUtil.success(null, "PIN changed successfully");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<StandardResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getUsername());
        return ResponseUtil.success(null, "OTP has been sent to your registered contact");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<StandardResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getUsername(), request.getOtp(), request.getNewPassword());
        return ResponseUtil.success(null, "Password reset successfully. Please login again.");
    }
}