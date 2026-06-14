package com.rikkei.bank.controller;

import com.rikkei.bank.annotation.LogExecutionTime;
import com.rikkei.bank.dto.transaction.request.TransferRequest;
import com.rikkei.bank.dto.common.response.StandardResponse;
import com.rikkei.bank.dto.transaction.response.TransactionHistoryResponse;
import com.rikkei.bank.dto.transaction.response.TransferResponse;
import com.rikkei.bank.entity.User;
import com.rikkei.bank.security.UserDetailsImpl;
import com.rikkei.bank.service.auth.IAuthService;
import com.rikkei.bank.service.transaction.ITransactionHistoryService;
import com.rikkei.bank.service.transaction.ITransferService;
import com.rikkei.bank.util.ResponseUtil;
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
    private final ITransferService transferService;
    private final ITransactionHistoryService transactionHistoryService;
    private final IAuthService authService;

    @LogExecutionTime
    @PostMapping("/transfer")
    public ResponseEntity<StandardResponse<TransferResponse>> transfer(@Valid @RequestBody TransferRequest request,
                                                                       @AuthenticationPrincipal UserDetailsImpl currentUser) {
        User user = authService.getCurrentUser(currentUser.getUsername());
        TransferResponse response = transferService.transfer(request, user);
        return ResponseUtil.success(response, "Transfer successful");
    }

    @LogExecutionTime
    @GetMapping("/history")
    public ResponseEntity<StandardResponse<Page<TransactionHistoryResponse>>> getTransactionHistory(
            @RequestParam String accountNumber,
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        User user = authService.getCurrentUser(currentUser.getUsername());
        Page<TransactionHistoryResponse> history = transactionHistoryService.getTransactionHistory(accountNumber, user, page, size);
        return ResponseUtil.success(history, "Get transaction history successfully");
    }
}