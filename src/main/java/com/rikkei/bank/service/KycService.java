package com.rikkei.bank.service;

import com.rikkei.bank.constants.KycStatus;
import com.rikkei.bank.dto.request.KycRequest;
import com.rikkei.bank.dto.response.KycResponse;
import com.rikkei.bank.entity.KycProfile;
import com.rikkei.bank.entity.User;
import com.rikkei.bank.exception.BadRequestException;
import com.rikkei.bank.exception.ResourceNotFoundException;
import com.rikkei.bank.repository.KycProfileRepository;
import com.rikkei.bank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class KycService {

    private final KycProfileRepository kycProfileRepository;
    private final UserRepository userRepository;

    // Trong thực tế sẽ dùng Cloudinary, nhưng hiện tại giả lập lưu URL
    @Value("${cloudinary.placeholder-url:https://via.placeholder.com/500}")
    private String placeholderUrl;

    @Transactional
    public KycResponse submitKyc(KycRequest request, User user) {
        // Kiểm tra đã có hồ sơ chưa
        if (kycProfileRepository.existsByUserAndStatus(user, KycStatus.PENDING)) {
            throw new BadRequestException("You already have a pending KYC request");
        }

        if (user.isKyc()) {
            throw new BadRequestException("You are already KYC verified");
        }

        // Trong thực tế: upload ảnh lên Cloudinary và lấy URL
        // Hiện tại dùng placeholder
        String frontUrl = placeholderUrl;
        String backUrl = placeholderUrl;
        String portraitUrl = placeholderUrl;

        KycProfile kycProfile = KycProfile.builder()
                .frontCitizenIdUrl(frontUrl)
                .backCitizenIdUrl(backUrl)
                .portraitUrl(portraitUrl)
                .status(KycStatus.PENDING)
                .submittedAt(LocalDateTime.now())
                .user(user)
                .build();

        kycProfileRepository.save(kycProfile);
        log.info("KYC submitted for user: {}", user.getUsername());

        return KycResponse.builder()
                .id(kycProfile.getId())
                .status(KycStatus.PENDING.toString())
                .message("KYC submitted successfully, waiting for approval")
                .submittedAt(LocalDateTime.now())
                .build();
    }

    @Transactional
    public KycResponse approveKyc(Long kycId, boolean approved, String rejectReason, User staffUser) {
        KycProfile kyc = kycProfileRepository.findById(kycId)
                .orElseThrow(() -> new ResourceNotFoundException("KYC record not found with id: " + kycId));

        if (kyc.getStatus() != KycStatus.PENDING) {
            throw new BadRequestException("This KYC request has already been processed");
        }

        if (approved) {
            kyc.setStatus(KycStatus.CONFIRM);
            // Cập nhật isKyc = true cho user
            User user = kyc.getUser();
            user.setKyc(true);
            userRepository.save(user);
            log.info("KYC approved for user: {} by staff: {}", user.getUsername(), staffUser.getUsername());
        } else {
            kyc.setStatus(KycStatus.REJECT);
            kyc.setRejectReason(rejectReason);
            log.info("KYC rejected for user: {} by staff: {}, reason: {}",
                    kyc.getUser().getUsername(), staffUser.getUsername(), rejectReason);
        }

        kyc.setReviewedAt(LocalDateTime.now());
        kycProfileRepository.save(kyc);

        return KycResponse.builder()
                .id(kyc.getId())
                .status(kyc.getStatus().toString())
                .message(approved ? "KYC approved successfully" : "KYC rejected: " + rejectReason)
                .rejectReason(approved ? null : rejectReason)
                .submittedAt(kyc.getSubmittedAt())
                .reviewedAt(LocalDateTime.now())
                .build();
    }

    public Page<KycResponse> getPendingKyc(Pageable pageable) {
        Page<KycProfile> pendingList = kycProfileRepository.findByStatus(KycStatus.PENDING, pageable);
        return pendingList.map(this::toResponse);
    }

    public KycResponse getMyKycStatus(User user) {
        KycProfile kyc = kycProfileRepository.findByUser(user)
                .orElse(null);

        if (kyc == null) {
            return KycResponse.builder()
                    .status("NOT_SUBMITTED")
                    .message("You haven't submitted KYC yet")
                    .build();
        }

        return toResponse(kyc);
    }

    private KycResponse toResponse(KycProfile kyc) {
        return KycResponse.builder()
                .id(kyc.getId())
                .status(kyc.getStatus().toString())
                .rejectReason(kyc.getRejectReason())
                .submittedAt(kyc.getSubmittedAt())
                .reviewedAt(kyc.getReviewedAt())
                .build();
    }
}