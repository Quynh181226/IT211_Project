package com.rikkei.bank.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycResponse {

    private Long id;
    private String status;  // PENDING, CONFIRM, REJECT
    private String message;
    private String rejectReason;  // Nếu bị từ chối
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
}