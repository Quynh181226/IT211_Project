package com.rikkei.bank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "token_blacklist")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenBlacklist {

    @Id
    @Column(name = "token", length = 500)
    private String token;  // Access Token bị thu hồi

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;  // Token hết hạn lúc nào (để xóa)
}