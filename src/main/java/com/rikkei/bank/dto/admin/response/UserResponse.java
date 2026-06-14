package com.rikkei.bank.dto.admin.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String fullName;
    private String username;
    private boolean isKyc;
    private boolean isLocked;
    private Set<String> roles;

    public UserResponse(Long id, String fullName, String username, boolean isKyc, boolean isLocked) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.isKyc = isKyc;
        this.isLocked = isLocked;
    }
}