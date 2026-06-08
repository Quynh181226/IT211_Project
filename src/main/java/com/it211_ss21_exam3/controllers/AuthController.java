package com.it211_ss21_exam3.controllers;

import com.it211_ss21_exam3.models.dtos.req.LoginReq;
import com.it211_ss21_exam3.models.dtos.req.RegisterReq;
import com.it211_ss21_exam3.models.dtos.req.TokenRefreshReq;
import com.it211_ss21_exam3.models.dtos.res.TokenRefreshRes;
import com.it211_ss21_exam3.models.dtos.wrapper.DataRes;
import com.it211_ss21_exam3.models.entities.User;
import com.it211_ss21_exam3.models.repositories.IUserRepository;
import com.it211_ss21_exam3.models.services.IAuthService;
import com.it211_ss21_exam3.security.principal.MyUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final IAuthService authService;
    private final IUserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> handleLogin(@Valid @RequestBody LoginReq req) {
        return ResponseEntity.status(HttpStatus.OK).body(
                DataRes.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(authService.login(req))
                        .build()
        );
    }

    @PostMapping("/register")
    public ResponseEntity<?> handleRegister(@Valid @RequestBody RegisterReq req) {
        authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                DataRes.builder()
                        .status(HttpStatus.CREATED)
                        .code(201)
                        .data("Register successfully")
                        .build()
        );
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> handleRefreshToken(@Valid @RequestBody TokenRefreshReq req) {
        TokenRefreshRes response = authService.refreshToken(req.getRefreshToken());
        return ResponseEntity.status(HttpStatus.OK).body(
                DataRes.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> handleLogout(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    DataRes.builder()
                            .status(HttpStatus.UNAUTHORIZED)
                            .code(401)
                            .data("User not authenticated")
                            .build()
            );
        }

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        authService.logout(user.getId());

        return ResponseEntity.status(HttpStatus.OK).body(
                DataRes.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data("Logout successful. All sessions terminated.")
                        .build()
        );
    }
}