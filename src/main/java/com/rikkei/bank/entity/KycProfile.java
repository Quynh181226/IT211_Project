package com.rikkei.bank.entity;

import com.rikkei.bank.constants.KycStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "kyc_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "front_citizen_id_url", length = 500)
    private String frontCitizenIdUrl;  // Ảnh CCCD mặt trước

    @Column(name = "back_citizen_id_url", length = 500)
    private String backCitizenIdUrl;   // Ảnh CCCD mặt sau

    @Column(name = "portrait_url", length = 500)
    private String portraitUrl;        // Ảnh chân dung

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private KycStatus status = KycStatus.PENDING;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;  // Lý do từ chối (nếu có)

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;  // Thời gian duyệt

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}