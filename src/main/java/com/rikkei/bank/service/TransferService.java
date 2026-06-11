package com.rikkei.bank.service;

import com.rikkei.bank.constants.TransactionStatus;
import com.rikkei.bank.constants.TransactionType;
import com.rikkei.bank.dto.request.TransferRequest;
import com.rikkei.bank.dto.response.TransferResponse;
import com.rikkei.bank.entity.Account;
import com.rikkei.bank.entity.Transaction;
import com.rikkei.bank.entity.User;
import com.rikkei.bank.exception.BadRequestException;
import com.rikkei.bank.exception.InsufficientBalanceException;
import com.rikkei.bank.repository.AccountRepository;
import com.rikkei.bank.repository.TransactionRepository;
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
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;

    private static final AtomicLong sequence = new AtomicLong(1);

    @Transactional(rollbackFor = Exception.class)
    public TransferResponse transfer(TransferRequest request, User currentUser) {
        // 1. Lấy tài khoản nguồn
        Account fromAccount = accountService.findByAccountNumber(request.getFromAccountNumber());

        // 2. Kiểm tra quyền sở hữu tài khoản nguồn
        if (!fromAccount.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You don't own this account");
        }

        // 3. Kiểm tra tài khoản còn hoạt động không
        if (!fromAccount.isActive()) {
            throw new BadRequestException("Source account is inactive");
        }

        // 4. Kiểm tra PIN giao dịch
        if (!passwordEncoder.matches(request.getPin(), currentUser.getPin())) {
            throw new BadRequestException("Invalid transaction PIN");
        }

        // 5. Kiểm tra số dư
        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance. Available: " + fromAccount.getBalance());
        }

        // 6. Xác định loại giao dịch và tài khoản đích
        TransactionType transactionType;
        Account toAccount;

        if (request.getToBankName() == null || request.getToBankName().isEmpty()) {
            // Nội bộ
            transactionType = TransactionType.INTERNAL;
            toAccount = accountService.findByAccountNumber(request.getToAccountNumber());

            if (!toAccount.isActive()) {
                throw new BadRequestException("Destination account is inactive");
            }
        } else {
            // Liên ngân hàng
            transactionType = TransactionType.EXTERNAL;

            // Tạo hoặc lấy tài khoản đích (tài khoản external)
            toAccount = accountRepository.findByAccountNumber(request.getToAccountNumber())
                    .orElseGet(() -> createExternalAccount(request));
        }

        // 7. Kiểm tra không được chuyển tiền cho chính mình
        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new BadRequestException("Cannot transfer to the same account");
        }

        // 8. Thực hiện chuyển tiền
        BigDecimal newFromBalance = fromAccount.getBalance().subtract(request.getAmount());
        BigDecimal newToBalance = toAccount.getBalance().add(request.getAmount());

        fromAccount.setBalance(newFromBalance);
        toAccount.setBalance(newToBalance);

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // 9. Tạo bản ghi giao dịch
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

        // 10. Trả về response
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
                .user(null)  // Không thuộc user nào trong hệ thống
                .build();

        return accountRepository.save(externalAccount);
    }

    private String generateTransactionCode() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String seq = String.format("%06d", sequence.getAndIncrement() % 1000000);
        return "TXN" + timestamp + seq;
    }
}