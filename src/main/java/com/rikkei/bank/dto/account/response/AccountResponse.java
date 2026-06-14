package com.rikkei.bank.dto.account.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private Long id;
    private String accountNumber;
    private String accountName;
    private BigDecimal balance;
    private String bankName;
    private Boolean isActive;
    private Long userId;
    private String userFullName;
}