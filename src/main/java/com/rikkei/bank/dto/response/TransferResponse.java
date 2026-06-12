package com.rikkei.bank.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {
    private String transactionCode;
    private String fromAccountNumber;
    private String toAccountNumber;
    private String toBankName;
    private BigDecimal amount;
    private BigDecimal fee;
    private String description;
    private String status;
    private LocalDateTime transactionTime;
    private BigDecimal remainingBalance;
}