//package com.rikkei.bank.controllers;
//
//import com.rikkei.bank.models.dtos.req.LoginReq;
//import com.rikkei.bank.models.dtos.req.RegisterReq;
//import com.rikkei.bank.models.dtos.req.TokenRefreshReq;
//import com.rikkei.bank.models.dtos.res.TokenRefreshRes;
//import com.rikkei.bank.models.dtos.wrapper.DataRes;
//import com.rikkei.bank.models.entities.User;
//import com.rikkei.bank.models.repositories.IUserRepository;
//import com.rikkei.bank.models.services.IAuthService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/v1/auth")
//@RequiredArgsConstructor
//public class AuthController {
//    private final IAuthService authService;
//    private final IUserRepository userRepository;
//
//    @PostMapping("/login")
//    public ResponseEntity<?> handleLogin(@Valid @RequestBody LoginReq req) {
//        return ResponseEntity.status(HttpStatus.OK).body(
//                DataRes.builder()
//                        .status(HttpStatus.OK)
//                        .code(200)
//                        .data(authService.login(req))
//                        .build()
//        );
//    }
//
//    @PostMapping("/register")
//    public ResponseEntity<?> handleRegister(@Valid @RequestBody RegisterReq req) {
//        authService.register(req);
//        return ResponseEntity.status(HttpStatus.CREATED).body(
//                DataRes.builder()
//                        .status(HttpStatus.CREATED)
//                        .code(201)
//                        .data("Register successfully")
//                        .build()
//        );
//    }
//
//    @PostMapping("/refresh-token")
//    public ResponseEntity<?> handleRefreshToken(@Valid @RequestBody TokenRefreshReq req) {
//        TokenRefreshRes response = authService.refreshToken(req.getRefreshToken());
//        return ResponseEntity.status(HttpStatus.OK).body(
//                DataRes.builder()
//                        .status(HttpStatus.OK)
//                        .code(200)
//                        .data(response)
//                        .build()
//        );
//    }
//
//    @PostMapping("/logout")
//    public ResponseEntity<?> handleLogout(@AuthenticationPrincipal UserDetails userDetails) {
//        if (userDetails == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
//                    DataRes.builder()
//                            .status(HttpStatus.UNAUTHORIZED)
//                            .code(401)
//                            .data("User not authenticated")
//                            .build()
//            );
//        }
//
//        User user = userRepository.findByUsername(userDetails.getUsername())
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        authService.logout(user.getId());
//
//        return ResponseEntity.status(HttpStatus.OK).body(
//                DataRes.builder()
//                        .status(HttpStatus.OK)
//                        .code(200)
//                        .data("Logout successful. All sessions terminated.")
//                        .build()
//        );
//    }
//}



package com.rikkei.bank.controller;

import com.rikkei.bank.dto.request.ChangePinRequest;
import com.rikkei.bank.dto.request.LoginRequest;
import com.rikkei.bank.dto.request.RefreshTokenRequest;
import com.rikkei.bank.dto.request.RegisterRequest;
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

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorization,
                                    @AuthenticationPrincipal UserDetailsImpl currentUser) {
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
}