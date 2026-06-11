package com.rikkei.bank.service;

import com.rikkei.bank.constants.RoleName;
import com.rikkei.bank.dto.request.LoginRequest;
import com.rikkei.bank.dto.request.RegisterRequest;
import com.rikkei.bank.dto.response.LoginResponse;
import com.rikkei.bank.dto.response.RefreshTokenResponse;
import com.rikkei.bank.entity.RefreshToken;
import com.rikkei.bank.entity.Role;
import com.rikkei.bank.entity.User;
import com.rikkei.bank.exception.BadRequestException;
import com.rikkei.bank.repository.RoleRepository;
import com.rikkei.bank.repository.UserRepository;
import com.rikkei.bank.security.JwtUtils;
import com.rikkei.bank.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final BlacklistService blacklistService;

    @Transactional
    public void register(RegisterRequest request) {
        // Kiểm tra username đã tồn tại chưa
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }

        // Gán role mặc định là CUSTOMER
        Set<Role> roles = new HashSet<>();
        Role customerRole = roleRepository.findByRoleName(RoleName.ROLE_CUSTOMER)
                .orElseThrow(() -> new BadRequestException("Default role not found"));
        roles.add(customerRole);

        // Tạo user mới
        User user = User.builder()
                .fullName(request.getFullName())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .isKyc(false)
                .isLocked(false)
                .build();

        userRepository.save(user);
        log.info("User registered successfully: {}", request.getUsername());
    }

    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // Kiểm tra tài khoản có bị khóa không
            if (userDetails.isLocked()) {
                throw new BadRequestException("Account is locked. Please contact admin.");
            }

            // Tạo token
            String accessToken = jwtUtils.generateAccessToken(userDetails.getUsername());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

            Set<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            log.info("User logged in: {}", request.getUsername());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .tokenType("Bearer")
                    .roles(roles)
                    .fullName(userDetails.getFullName())
                    .username(userDetails.getUsername())
                    .build();

        } catch (AuthenticationException e) {
            log.error("Login failed for user: {}", request.getUsername());
            throw new BadRequestException("Invalid username or password");
        }
    }

    public RefreshTokenResponse refreshToken(String refreshTokenString) {
        // Tìm và xác thực refresh token
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenString);
        refreshTokenService.verifyExpiration(refreshToken);

        // Lấy user
        User user = refreshToken.getUser();

        // Tạo access token mới
        String newAccessToken = jwtUtils.generateAccessToken(user.getUsername());

        // Rotate refresh token (tạo mới, revoke cũ)
        refreshTokenService.revokeAllByUser(user.getId());
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

        log.info("Refreshed token for user: {}", user.getUsername());

        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .build();
    }

    @Transactional
    public void logout(String accessToken, Long userId) {
        // Thêm access token vào blacklist
        blacklistService.blacklistToken(accessToken);

        // Xóa tất cả refresh token của user
        refreshTokenService.revokeAllByUser(userId);

        log.info("User logged out, userId: {}", userId);
    }

    public User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found"));
    }
}