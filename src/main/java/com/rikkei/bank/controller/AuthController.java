package com.rikkei.bank.controller;

import com.rikkei.bank.annotation.LogExecutionTime;
import com.rikkei.bank.dto.request.*;
import com.rikkei.bank.dto.response.LoginResponse;
import com.rikkei.bank.dto.response.RefreshTokenResponse;
import com.rikkei.bank.entity.User;
import com.rikkei.bank.security.UserDetailsImpl;
import com.rikkei.bank.service.AuthService;
import com.rikkei.bank.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User registered successfully");
        response.put("username", request.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @LogExecutionTime
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @LogExecutionTime
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorization,
                                    @AuthenticationPrincipal UserDetailsImpl currentUser) {
        if (currentUser == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "User not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            authService.logout(token, currentUser.getId());
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout successful");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-pin")
    public ResponseEntity<?> changePin(@Valid @RequestBody ChangePinRequest request,
                                       @AuthenticationPrincipal UserDetailsImpl currentUser) {
        User user = authService.getCurrentUser(currentUser.getUsername());
        userService.changePin(user, request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "PIN changed successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getUsername());
        Map<String, String> response = new HashMap<>();
        response.put("message", "OTP has been sent to your registered contact");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getUsername(), request.getOtp(), request.getNewPassword());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset successfully. Please login again.");
        return ResponseEntity.ok(response);
    }
}