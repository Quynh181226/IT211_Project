package com.rikkei.bank.service.auth.impl;

import com.rikkei.bank.constants.RoleName;
import com.rikkei.bank.dto.auth.request.LoginRequest;
import com.rikkei.bank.dto.auth.request.RegisterRequest;
import com.rikkei.bank.dto.auth.response.LoginResponse;
import com.rikkei.bank.dto.auth.response.RefreshTokenResponse;
import com.rikkei.bank.entity.RefreshToken;
import com.rikkei.bank.entity.Role;
import com.rikkei.bank.entity.User;
import com.rikkei.bank.exception.BadRequestException;
import com.rikkei.bank.repository.RoleRepository;
import com.rikkei.bank.repository.UserRepository;
import com.rikkei.bank.security.JwtUtils;
import com.rikkei.bank.security.UserDetailsImpl;
import com.rikkei.bank.service.auth.IAuthService;
import com.rikkei.bank.service.email.IEmailService;
import com.rikkei.bank.service.otp.IOtpService;
import com.rikkei.bank.service.token.IRedisBlacklistService;
import com.rikkei.bank.service.token.IRefreshTokenService;
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
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final IRefreshTokenService refreshTokenService;
    private final IOtpService otpService;
    private final IRedisBlacklistService redisBlacklistService;
    private final IEmailService emailService;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }

        Set<Role> roles = new HashSet<>();
        Role customerRole = roleRepository.findByRoleName(RoleName.ROLE_CUSTOMER)
                .orElseThrow(() -> new BadRequestException("Default role not found"));
        roles.add(customerRole);

        User user = User.builder()
                .fullName(request.getFullName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .isKyc(false)
                .isLocked(false)
                .build();

        userRepository.save(user);
        log.info("User registered successfully: {}", request.getUsername());
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            if (userDetails.isLocked()) {
                throw new BadRequestException("Account is locked. Please contact admin.");
            }

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

    @Override
    public RefreshTokenResponse refreshToken(String refreshTokenString) {
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenString);
        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();

        String newAccessToken = jwtUtils.generateAccessToken(user.getUsername());

        refreshTokenService.revokeAllByUser(user.getId());
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

        log.info("Refreshed token for user: {}", user.getUsername());

        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .build();
    }

    @Override
    @Transactional
    public void logout(String accessToken, Long userId) {
        long expirationMillis = jwtUtils.getExpirationMillisFromToken(accessToken);
        redisBlacklistService.blacklistTokenWithJwtExpiration(accessToken, expirationMillis);
        refreshTokenService.revokeAllByUser(userId);
        log.info("User logged out, userId: {}", userId);
    }

    @Override
    public User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found"));
    }

    @Override
    public void forgotPassword(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found with username: " + username));

        String otp = otpService.generateOtp(username);
        emailService.sendOtp(user.getEmail(), otp);

        log.info("OTP sent to email: {}", user.getEmail());
    }

    @Override
    public void resetPassword(String username, String otp, String newPassword) {
        if (!otpService.verifyOtp(username, otp)) {
            throw new BadRequestException("Invalid or expired OTP");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        otpService.removeOtp(username);
        refreshTokenService.revokeAllByUser(user.getId());

        log.info("Password reset successfully for user: {}", username);
    }
}