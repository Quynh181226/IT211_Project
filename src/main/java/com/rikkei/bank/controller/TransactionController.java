package com.rikkei.bank.controller;

import com.rikkei.bank.annotation.LogExecutionTime;
import com.rikkei.bank.dto.request.TransferRequest;
import com.rikkei.bank.dto.response.TransactionHistoryResponse;
import com.rikkei.bank.dto.response.TransferResponse;
import com.rikkei.bank.entity.User;
import com.rikkei.bank.security.UserDetailsImpl;
import com.rikkei.bank.service.AuthService;
import com.rikkei.bank.service.TransactionHistoryService;
import com.rikkei.bank.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransferService transferService;
    private final TransactionHistoryService transactionHistoryService;
    private final AuthService authService;

    @LogExecutionTime
    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request,
                                                     @AuthenticationPrincipal UserDetailsImpl currentUser) {
        User user = authService.getCurrentUser(currentUser.getUsername());
        TransferResponse response = transferService.transfer(request, user);
        return ResponseEntity.ok(response);
    }

    @LogExecutionTime
    @GetMapping("/history")
    public ResponseEntity<Page<TransactionHistoryResponse>> getTransactionHistory(
            @RequestParam String accountNumber,
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PageableDefault(size = 20) Pageable pageable) {

        User user = authService.getCurrentUser(currentUser.getUsername());
        Page<TransactionHistoryResponse> history = transactionHistoryService.getTransactionHistory(accountNumber, user, pageable);
        return ResponseEntity.ok(history);
    }
}