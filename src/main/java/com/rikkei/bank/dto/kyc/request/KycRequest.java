package com.rikkei.bank.dto.kyc.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycRequest {
    @NotNull(message = "Front citizen ID image is required")
    private MultipartFile frontCitizenId;

    @NotNull(message = "Back citizen ID image is required")
    private MultipartFile backCitizenId;

    @NotNull(message = "Portrait image is required")
    private MultipartFile portrait;
}