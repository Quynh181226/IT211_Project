package com.rikkei.bank.controller;

import com.rikkei.bank.dto.common.response.StandardResponse;
import com.rikkei.bank.dto.admin.response.UserResponse;
import com.rikkei.bank.service.user.IUserService;
import com.rikkei.bank.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    private final IUserService userService;

    @GetMapping("/users")
    public ResponseEntity<StandardResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<UserResponse> users = userService.getAllUsers(page, size);
        return ResponseUtil.success(users, "Get all users successfully");
    }

    @PutMapping("/users/{userId}/lock")
    public ResponseEntity<StandardResponse<Void>> lockUser(@PathVariable Long userId, @RequestParam boolean lock) {
        userService.lockUser(userId, lock);
        String message = lock ? "User locked successfully" : "User unlocked successfully";
        return ResponseUtil.success(null, message);
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<StandardResponse<Void>> assignRole(@PathVariable Long userId, @RequestParam String roleName) {
        userService.assignRole(userId, roleName);
        return ResponseUtil.success(null, "Role assigned successfully");
    }
}