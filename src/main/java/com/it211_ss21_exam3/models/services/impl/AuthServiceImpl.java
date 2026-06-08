package com.it211_ss21_exam3.models.services.impl;

import com.it211_ss21_exam3.exceptions.HttpBadRequestException;
import com.it211_ss21_exam3.models.constants.RoleName;
import com.it211_ss21_exam3.models.dtos.req.LoginReq;
import com.it211_ss21_exam3.models.dtos.req.RegisterReq;
import com.it211_ss21_exam3.models.dtos.res.JwtRes;
import com.it211_ss21_exam3.models.dtos.res.TokenRefreshRes;
import com.it211_ss21_exam3.models.entities.RefreshToken;
import com.it211_ss21_exam3.models.entities.Role;
import com.it211_ss21_exam3.models.entities.User;
import com.it211_ss21_exam3.models.repositories.IUserRepository;
import com.it211_ss21_exam3.models.services.IAuthService;
import com.it211_ss21_exam3.models.services.IRefreshTokenService;
import com.it211_ss21_exam3.models.services.IRoleService;
import com.it211_ss21_exam3.security.jwt.JwtUtils;
import com.it211_ss21_exam3.security.principal.MyUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {
    private final IRoleService roleService;
    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final IRefreshTokenService refreshTokenService;

    @Override
    public void register(RegisterReq req) {
        Set<Role> roles = new HashSet<>();
        roles.add(roleService.findByRoleName(RoleName.ROLE_USER));
        User user = User.builder()
                .fullName(req.getFullName())
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .roles(roles)
                .build();
        userRepository.save(user);
    }

    @Override
    public JwtRes login(LoginReq req) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );
        }
        catch (AuthenticationException e) {
            throw new HttpBadRequestException("Username or password is incorrect");
        }

        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();

        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new HttpBadRequestException("User not found"));

        String accessToken = jwtUtils.generateToken(userDetails.getUsername());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return JwtRes.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .roles(userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet()))
                .build();
    }

    public TokenRefreshRes refreshToken(String refreshTokenString) {
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenString);
        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();

        String newAccessToken = jwtUtils.generateToken(user.getUsername());

        refreshTokenService.revokeAllByUser(user.getId());
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new TokenRefreshRes(newAccessToken, newRefreshToken.getToken());
    }

    public void logout(Long userId) {
        refreshTokenService.revokeAllByUser(userId);
    }
}