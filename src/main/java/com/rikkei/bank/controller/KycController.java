package com.rikkei.bank.controller;

import com.rikkei.bank.dto.request.KycRequest;
import com.rikkei.bank.dto.response.KycResponse;
import com.rikkei.bank.entity.User;
import com.rikkei.bank.security.UserDetailsImpl;
import com.rikkei.bank.service.AuthService;
import com.rikkei.bank.service.KycService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/kyc")
@RequiredArgsConstructor
public class KycController {

    private final KycService kycService;
    private final AuthService authService;

    @PostMapping("/submit")
    public ResponseEntity<KycResponse> submitKyc(@Valid @RequestBody KycRequest request,
                                                 @AuthenticationPrincipal UserDetailsImpl currentUser) {
        User user = authService.getCurrentUser(currentUser.getUsername());
        KycResponse response = kycService.submitKyc(request, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-status")
    public ResponseEntity<KycResponse> getMyKycStatus(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        User user = authService.getCurrentUser(currentUser.getUsername());
        KycResponse response = kycService.getMyKycStatus(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<Page<KycResponse>> getPendingKyc(@PageableDefault(size = 10) Pageable pageable) {
        Page<KycResponse> pendingList = kycService.getPendingKyc(pageable);
        return ResponseEntity.ok(pendingList);
    }

    @PutMapping("/{kycId}/approve")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<KycResponse> approveKyc(@PathVariable Long kycId,
                                                  @RequestParam boolean approved,
                                                  @RequestParam(required = false) String rejectReason,
                                                  @AuthenticationPrincipal UserDetailsImpl currentUser) {
        User staffUser = authService.getCurrentUser(currentUser.getUsername());
        KycResponse response = kycService.approveKyc(kycId, approved, rejectReason, staffUser);
        return ResponseEntity.ok(response);
    }
}