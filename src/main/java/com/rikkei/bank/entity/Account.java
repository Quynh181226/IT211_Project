package com.rikkei.bank.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", unique = true, nullable = false, length = 20)
    private String accountNumber;  // Số tài khoản (duy nhất)

    @Column(name = "account_name", nullable = false)
    private String accountName;  // Tên chủ tài khoản

    @Column(name = "balance", nullable = false)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;  // Số dư

    @Column(name = "bank_name")
    private String bankName;  // Tên ngân hàng (null nếu là tài khoản Rikkei Bank)

    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = true;  // Tài khoản còn hoạt động không

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}