package com.rikkei.bank.service;

import com.rikkei.bank.constants.RoleName;
import com.rikkei.bank.dto.request.ChangePinRequest;
import com.rikkei.bank.dto.response.UserResponse;
import com.rikkei.bank.entity.Role;
import com.rikkei.bank.entity.User;
import com.rikkei.bank.exception.BadRequestException;
import com.rikkei.bank.exception.ResourceNotFoundException;
import com.rikkei.bank.repository.RoleRepository;
import com.rikkei.bank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void changePin(User user, ChangePinRequest request) {
        // Kiểm tra old PIN
        if (!passwordEncoder.matches(request.getOldPin(), user.getPin())) {
            throw new BadRequestException("Old PIN is incorrect");
        }

        // Kiểm tra new PIN và confirm PIN
        if (!request.getNewPin().equals(request.getConfirmPin())) {
            throw new BadRequestException("New PIN and confirm PIN do not match");
        }

        // Cập nhật PIN mới
        String encodedNewPin = passwordEncoder.encode(request.getNewPin());
        user.setPin(encodedNewPin);
        userRepository.save(user);

        log.info("PIN changed for user: {}", user.getUsername());
    }

    @Transactional
    public void setInitialPin(User user, String pin) {
        String encodedPin = passwordEncoder.encode(pin);
        user.setPin(encodedPin);
        userRepository.save(user);
        log.info("Initial PIN set for user: {}", user.getUsername());
    }

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::toResponse);
    }

    @Transactional
    public void lockUser(Long userId, boolean lock) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setLocked(lock);
        userRepository.save(user);

        log.info("User {} {}", user.getUsername(), lock ? "locked" : "unlocked");
    }

    @Transactional
    public void assignRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        RoleName roleEnum;
        try {
            roleEnum = RoleName.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role name: " + roleName);
        }

        Role role = roleRepository.findByRoleName(roleEnum)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));

        user.getRoles().add(role);
        userRepository.save(user);

        log.info("Role {} assigned to user: {}", roleName, user.getUsername());
    }

    private UserResponse toResponse(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getRoleName().name())
                .collect(Collectors.toSet());

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .isKyc(user.isKyc())
                .isLocked(user.isLocked())
                .roles(roles)
                .build();
    }
}