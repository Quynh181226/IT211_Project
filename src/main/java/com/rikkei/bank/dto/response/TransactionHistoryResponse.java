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
    private String type;  // "DEBIT" (trừ tiền) hoặc "CREDIT" (cộng tiền)
    private String counterPartyAccount;  // Tài khoản đối diện
    private String counterPartyName;     // Tên chủ tài khoản đối diện
    private String description;
    private BigDecimal balanceAfter;     // Số dư sau giao dịch
    private String transactionType;      // "INTERNAL" hoặc "EXTERNAL"
}