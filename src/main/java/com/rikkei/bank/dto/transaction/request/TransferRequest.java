package com.rikkei.bank.dto.transaction.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
    @NotBlank(message = "From account number cannot be blank")
    private String fromAccountNumber;

    @NotBlank(message = "To account number cannot be blank")
    private String toAccountNumber;

    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String description;

    private String toBankName;

    @NotBlank(message = "PIN cannot be blank")
    @Size(min = 4, max = 6, message = "PIN must be 4-6 characters")
    private String pin;
}