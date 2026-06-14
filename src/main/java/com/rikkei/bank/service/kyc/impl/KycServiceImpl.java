package com.rikkei.bank.service.kyc.impl;

import com.cloudinary.Cloudinary;
import com.rikkei.bank.constants.KycStatus;
import com.rikkei.bank.dto.kyc.request.KycRequest;
import com.rikkei.bank.dto.kyc.response.KycResponse;
import com.rikkei.bank.entity.KycProfile;
import com.rikkei.bank.entity.User;
import com.rikkei.bank.exception.BadRequestException;
import com.rikkei.bank.exception.ResourceNotFoundException;
import com.rikkei.bank.repository.KycProfileRepository;
import com.rikkei.bank.repository.UserRepository;
import com.rikkei.bank.service.kyc.IKycService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KycServiceImpl implements IKycService {

    private final KycProfileRepository kycProfileRepository;
    private final UserRepository userRepository;
    private final Cloudinary cloudinary;

    @Override
    @Transactional
    public KycResponse submitKyc(KycRequest request, User user) throws IOException {
        if (kycProfileRepository.existsByUserAndStatus(user, KycStatus.PENDING)) {
            throw new BadRequestException("You already have a pending KYC request");
        }
        if (user.isKyc()) {
            throw new BadRequestException("You are already KYC verified");
        }

        String frontUrl = uploadToCloudinary(request.getFrontCitizenId(), "kyc/front");
        String backUrl = uploadToCloudinary(request.getBackCitizenId(), "kyc/back");
        String portraitUrl = uploadToCloudinary(request.getPortrait(), "kyc/portrait");

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

    private String uploadToCloudinary(org.springframework.web.multipart.MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty or null");
        }

        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                Map.of(
                        "folder", folder,
                        "resource_type", "auto"
                ));

        return (String) uploadResult.get("secure_url");
    }

    @Override
    @Transactional
    public KycResponse approveKyc(Long kycId, boolean approved, String rejectReason, User staffUser) {
        KycProfile kyc = kycProfileRepository.findById(kycId)
                .orElseThrow(() -> new ResourceNotFoundException("KYC record not found with id: " + kycId));

        if (kyc.getStatus() != KycStatus.PENDING) {
            throw new BadRequestException("This KYC request has already been processed");
        }

        if (approved) {
            kyc.setStatus(KycStatus.CONFIRM);
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

    @Override
    public Page<KycResponse> getPendingKyc(Pageable pageable) {
        Page<KycProfile> pendingList = kycProfileRepository.findByStatus(KycStatus.PENDING, pageable);
        return pendingList.map(this::toResponse);
    }

    @Override
    public KycResponse getMyKycStatus(User user) {
        KycProfile kyc = kycProfileRepository.findByUser(user).orElse(null);
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