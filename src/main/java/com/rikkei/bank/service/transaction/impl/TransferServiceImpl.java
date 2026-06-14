package com.rikkei.bank.service.transaction.impl;

import com.rikkei.bank.constants.TransactionStatus;
import com.rikkei.bank.constants.TransactionType;
import com.rikkei.bank.dto.transaction.request.TransferRequest;
import com.rikkei.bank.dto.transaction.response.TransferResponse;
import com.rikkei.bank.entity.Account;
import com.rikkei.bank.entity.Transaction;
import com.rikkei.bank.entity.User;
import com.rikkei.bank.exception.BadRequestException;
import com.rikkei.bank.exception.InsufficientBalanceException;
import com.rikkei.bank.exception.OptimisticLockException;
import com.rikkei.bank.repository.AccountRepository;
import com.rikkei.bank.repository.TransactionRepository;
import com.rikkei.bank.service.account.IAccountService;
import com.rikkei.bank.service.transaction.ITransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferServiceImpl implements ITransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final IAccountService accountService;
    private final PasswordEncoder passwordEncoder;

    private static final AtomicLong sequence = new AtomicLong(1);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransferResponse transfer(TransferRequest request, User currentUser) {
        Account fromAccount = accountService.findByAccountNumber(request.getFromAccountNumber());

        if (!fromAccount.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You don't own this account");
        }

        if (!fromAccount.isActive()) {
            throw new BadRequestException("Source account is inactive");
        }

        if (!passwordEncoder.matches(request.getPin(), currentUser.getPin())) {
            throw new BadRequestException("Invalid transaction PIN");
        }

        Long currentVersion = fromAccount.getVersion();

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance. Available: " + fromAccount.getBalance());
        }

        TransactionType transactionType;
        Account toAccount;

        if (request.getToBankName() == null || request.getToBankName().isEmpty()) {
            transactionType = TransactionType.INTERNAL;
            toAccount = accountService.findByAccountNumber(request.getToAccountNumber());
            if (!toAccount.isActive()) {
                throw new BadRequestException("Destination account is inactive");
            }
        } else {
            transactionType = TransactionType.EXTERNAL;
            toAccount = accountRepository.findByAccountNumber(request.getToAccountNumber())
                    .orElseGet(() -> createExternalAccount(request));
        }

        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new BadRequestException("Cannot transfer to the same account");
        }

        BigDecimal newFromBalance = fromAccount.getBalance().subtract(request.getAmount());
        BigDecimal newToBalance = toAccount.getBalance().add(request.getAmount());

        int updatedRows = accountRepository.updateBalanceAndVersion(
                fromAccount.getId(),
                newFromBalance,
                currentVersion
        );

        if (updatedRows == 0) {
            throw new OptimisticLockException("Transaction failed due to concurrent modification. Please retry.");
        }

        toAccount.setBalance(newToBalance);
        accountRepository.save(toAccount);

        fromAccount.setBalance(newFromBalance);

        String transactionCode = generateTransactionCode();

        Transaction transaction = Transaction.builder()
                .transactionCode(transactionCode)
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(request.getAmount())
                .description(request.getDescription())
                .transactionType(transactionType)
                .status(TransactionStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);

        log.info("[TRANSFER] User: {} transferred {} from {} to {}, type: {}",
                currentUser.getUsername(),
                request.getAmount(),
                fromAccount.getAccountNumber(),
                toAccount.getAccountNumber(),
                transactionType);

        return TransferResponse.builder()
                .transactionCode(transactionCode)
                .fromAccountNumber(fromAccount.getAccountNumber())
                .toAccountNumber(toAccount.getAccountNumber())
                .toBankName(toAccount.getBankName())
                .amount(request.getAmount())
                .fee(BigDecimal.ZERO)
                .description(request.getDescription())
                .status("SUCCESS")
                .transactionTime(LocalDateTime.now())
                .remainingBalance(newFromBalance)
                .build();
    }

    private Account createExternalAccount(TransferRequest request) {
        Account externalAccount = Account.builder()
                .accountNumber(request.getToAccountNumber())
                .accountName("External Account")
                .balance(BigDecimal.ZERO)
                .bankName(request.getToBankName())
                .isActive(true)
                .user(null)
                .build();
        return accountRepository.save(externalAccount);
    }

    private String generateTransactionCode() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String seq = String.format("%06d", sequence.getAndIncrement() % 1000000);
        return "TXN" + timestamp + seq;
    }
}