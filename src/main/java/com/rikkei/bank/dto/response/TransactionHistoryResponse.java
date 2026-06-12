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
public class TransactionHistoryResponse {
    private String transactionCode;
    private LocalDateTime transactionDate;
    private BigDecimal amount;
    private String type;
    private String counterPartyAccount;
    private String counterPartyName;
    private String description;
    private BigDecimal balanceAfter;
    private String transactionType;
}