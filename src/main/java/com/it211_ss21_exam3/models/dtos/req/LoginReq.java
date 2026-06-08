package com.it211_ss21_exam3.models.dtos.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LoginReq {
    @NotBlank(message = "username must be not empty")
    private String username;
    @NotBlank(message = "password must be not empty")
    private String password;
}
