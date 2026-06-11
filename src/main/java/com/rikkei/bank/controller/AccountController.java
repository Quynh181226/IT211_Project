package com.rikkei.bank.controller;

import com.rikkei.bank.dto.request.OpenAccountRequest;
import com.rikkei.bank.dto.response.AccountResponse;
import com.rikkei.bank.dto.response.BalanceResponse;
import com.rikkei.bank.entity.User;
import com.rikkei.bank.security.UserDetailsImpl;
import com.rikkei.bank.service.AccountService;
import com.rikkei.bank.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AuthService authService;

    @PostMapping("/open")
    public ResponseEntity<AccountResponse> openAccount(@Valid @RequestBody OpenAccountRequest request,
                                                       @AuthenticationPrincipal UserDetailsImpl currentUser) {
        User user = authService.getCurrentUser(currentUser.getUsername());
        AccountResponse response = accountService.openAccount(user, request.getAccountName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-accounts")
    public ResponseEntity<Page<AccountResponse>> getMyAccounts(@AuthenticationPrincipal UserDetailsImpl currentUser,
                                                               @PageableDefault(size = 10) Pageable pageable) {
        User user = authService.getCurrentUser(currentUser.getUsername());
        Page<AccountResponse> accounts = accountService.getMyAccounts(user, pageable);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable String accountNumber,
                                                      @AuthenticationPrincipal UserDetailsImpl currentUser) {
        User user = authService.getCurrentUser(currentUser.getUsername());
        BigDecimal balance = accountService.getBalance(accountNumber, user);

        BalanceResponse response = BalanceResponse.builder()
                .accountNumber(accountNumber)
                .balance(balance)
                .build();

        return ResponseEntity.ok(response);
    }
}