package com.rikkei.bank.controller;

import com.rikkei.bank.dto.kyc.request.KycRequest;
import com.rikkei.bank.dto.kyc.response.KycResponse;
import com.rikkei.bank.dto.common.response.StandardResponse;
import com.rikkei.bank.entity.User;
import com.rikkei.bank.security.UserDetailsImpl;
import com.rikkei.bank.service.auth.IAuthService;
import com.rikkei.bank.service.kyc.IKycService;
import com.rikkei.bank.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/kyc")
@RequiredArgsConstructor
public class KycController {
    private final IKycService kycService;
    private final IAuthService authService;

    @PostMapping("/upload")
    public ResponseEntity<StandardResponse<KycResponse>> submitKyc(
            @Valid @ModelAttribute KycRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) throws IOException {

        User user = authService.getCurrentUser(currentUser.getUsername());
        KycResponse response = kycService.submitKyc(request, user);
        return ResponseUtil.success(response, "KYC submitted successfully");
    }

    @GetMapping("/my-status")
    public ResponseEntity<StandardResponse<KycResponse>> getMyKycStatus(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        User user = authService.getCurrentUser(currentUser.getUsername());
        KycResponse response = kycService.getMyKycStatus(user);
        return ResponseUtil.success(response, "Get KYC status successfully");
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<StandardResponse<Page<KycResponse>>> getPendingKyc(@PageableDefault(size = 10) Pageable pageable) {
        Page<KycResponse> pendingList = kycService.getPendingKyc(pageable);
        return ResponseUtil.success(pendingList, "Get pending KYC list successfully");
    }

    @PutMapping("/{kycId}/approve")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<StandardResponse<KycResponse>> approveKyc(@PathVariable Long kycId,
                                                                    @RequestParam boolean approved,
                                                                    @RequestParam(required = false) String rejectReason,
                                                                    @AuthenticationPrincipal UserDetailsImpl currentUser) {
        User staffUser = authService.getCurrentUser(currentUser.getUsername());
        KycResponse response = kycService.approveKyc(kycId, approved, rejectReason, staffUser);
        String message = approved ? "KYC approved successfully" : "KYC rejected: " + rejectReason;
        return ResponseUtil.success(response, message);
    }
}