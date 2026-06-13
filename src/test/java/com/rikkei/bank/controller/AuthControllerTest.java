package com.rikkei.bank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rikkei.bank.dto.request.LoginRequest;
import com.rikkei.bank.dto.request.RegisterRequest;
import com.rikkei.bank.dto.request.ResetPasswordRequest;
import com.rikkei.bank.dto.response.LoginResponse;
import com.rikkei.bank.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController Unit Tests")
class AuthControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    void register_ValidRequest_Returns201() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Test User").username("testuser").password("123456").build();
        doNothing().when(authService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

    @Test
    void login_ValidCredentials_Returns200() throws Exception {
        LoginRequest request = LoginRequest.builder().username("testuser").password("123456").build();
        LoginResponse response = LoginResponse.builder()
                .accessToken("access-token").refreshToken("refresh-token")
                .tokenType("Bearer").roles(Set.of("ROLE_CUSTOMER"))
                .fullName("Test User").username("testuser").build();
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"));
    }

    @Test
    void logout_ValidToken_Returns200() throws Exception {
        doNothing().when(authService).logout(any(), any());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }

    @Test
    void forgotPassword_ValidUsername_Returns200() throws Exception {
        doNothing().when(authService).forgotPassword(any());

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void resetPassword_ValidRequest_Returns200() throws Exception {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .username("testuser").otp("123456").newPassword("newpass123").build();
        doNothing().when(authService).resetPassword(any(), any(), any());

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}