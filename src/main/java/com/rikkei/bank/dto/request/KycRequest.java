package com.rikkei.bank.dto.request;

import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "Front citizen ID image is required")
    private String frontCitizenId;  // base64 hoặc multipart, tùy cách implement

    @NotBlank(message = "Back citizen ID image is required")
    private String backCitizenId;

    @NotBlank(message = "Portrait image is required")
    private String portrait;
}