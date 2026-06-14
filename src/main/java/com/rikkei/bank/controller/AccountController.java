package com.rikkei.bank.controller;

import com.rikkei.bank.annotation.LogExecutionTime;
import com.rikkei.bank.dto.account.request.OpenAccountRequest;
import com.rikkei.bank.dto.account.response.AccountResponse;
import com.rikkei.bank.dto.account.response.BalanceResponse;
import com.rikkei.bank.dto.common.response.StandardResponse;
import com.rikkei.bank.entity.User;
import com.rikkei.bank.security.UserDetailsImpl;
import com.rikkei.bank.service.account.IAccountService;
import com.rikkei.bank.service.auth.IAuthService;
import com.rikkei.bank.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final IAccountService accountService;
    private final IAuthService authService;

    @LogExecutionTime
    @PostMapping("/open")
    public ResponseEntity<StandardResponse<AccountResponse>> openAccount(@Valid @RequestBody OpenAccountRequest request,
                                                                         @AuthenticationPrincipal UserDetailsImpl currentUser) {
        User user = authService.getCurrentUser(currentUser.getUsername());
        AccountResponse response = accountService.openAccount(user, request.getAccountName());
        return ResponseUtil.success(response, "Account opened successfully");
    }

    @LogExecutionTime
    @GetMapping("/my-accounts")
    public ResponseEntity<StandardResponse<Page<AccountResponse>>> getMyAccounts(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = authService.getCurrentUser(currentUser.getUsername());
        Page<AccountResponse> accounts = accountService.getMyAccounts(user, page, size);
        return ResponseUtil.success(accounts, "Get accounts successfully");
    }

    @LogExecutionTime
    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<StandardResponse<BalanceResponse>> getBalance(@PathVariable String accountNumber,
                                                                        @AuthenticationPrincipal UserDetailsImpl currentUser) {
        User user = authService.getCurrentUser(currentUser.getUsername());
        BigDecimal balance = accountService.getBalance(accountNumber, user);

        BalanceResponse response = BalanceResponse.builder()
                .accountNumber(accountNumber)
                .balance(balance)
                .build();

        return ResponseUtil.success(response, "Get balance successfully");
    }
}