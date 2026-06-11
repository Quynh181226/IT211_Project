package com.rikkei.bank.controller;

import com.rikkei.bank.dto.response.UserResponse;
import com.rikkei.bank.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getAllUsers(@PageableDefault(size = 20) Pageable pageable) {
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{userId}/lock")
    public ResponseEntity<?> lockUser(@PathVariable Long userId, @RequestParam boolean lock) {
        userService.lockUser(userId, lock);

        Map<String, String> response = new HashMap<>();
        response.put("message", lock ? "User locked successfully" : "User unlocked successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<?> assignRole(@PathVariable Long userId, @RequestParam String roleName) {
        userService.assignRole(userId, roleName);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Role assigned successfully");
        return ResponseEntity.ok(response);
    }
}