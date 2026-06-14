package com.rikkei.bank.service.account.impl;

import com.rikkei.bank.dto.account.response.AccountResponse;
import com.rikkei.bank.entity.Account;
import com.rikkei.bank.entity.User;
import com.rikkei.bank.exception.BadRequestException;
import com.rikkei.bank.exception.ResourceNotFoundException;
import com.rikkei.bank.repository.AccountRepository;
import com.rikkei.bank.service.account.IAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements IAccountService {   // ← implements interface

    private final AccountRepository accountRepository;

    @Override   // ← thêm @Override cho mỗi method
    @Transactional
    public AccountResponse openAccount(User user, String accountName) {
        if (!user.isKyc()) {
            throw new BadRequestException("Please complete KYC before opening an account");
        }

        String accountNumber = generateAccountNumber();
        Account account = Account.builder()
                .accountNumber(accountNumber)
                .accountName(accountName)
                .balance(BigDecimal.ZERO)
                .bankName(null)
                .isActive(true)
                .user(user)
                .build();

        Account savedAccount = accountRepository.save(account);
        log.info("New account opened for user: {}, accountNumber: {}", user.getUsername(), accountNumber);
        return toResponse(savedAccount);
    }

    @Override
    public Page<AccountResponse> getMyAccounts(User user, Pageable pageable) {
        Page<Account> accounts = accountRepository.findByUser(user, pageable);
        return accounts.map(this::toResponse);
    }

    @Override
    public BigDecimal getBalance(String accountNumber, User user) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));
        if (!account.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't own this account");
        }
        return account.getBalance();
    }

    @Override
    public Account findByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));
    }

    private String generateAccountNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new SecureRandom().nextInt(10000));
        return "890" + timestamp + random;
    }

    private AccountResponse toResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountName(account.getAccountName())
                .balance(account.getBalance())
                .bankName(account.getBankName())
                .isActive(account.isActive())
                .userId(account.getUser().getId())
                .userFullName(account.getUser().getFullName())
                .build();
    }
}