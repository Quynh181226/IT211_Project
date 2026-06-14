package com.rikkei.bank.dto.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePinRequest {
    @NotBlank(message = "Old PIN cannot be blank")
    @Size(min = 4, max = 6, message = "PIN must be 4-6 characters")
    private String oldPin;

    @NotBlank(message = "New PIN cannot be blank")
    @Size(min = 4, max = 6, message = "PIN must be 4-6 characters")
    private String newPin;

    @NotBlank(message = "Confirm PIN cannot be blank")
    private String confirmPin;
}