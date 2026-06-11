package com.rikkei.bank.service;

import com.rikkei.bank.dto.response.TransactionHistoryResponse;
import com.rikkei.bank.entity.Account;
import com.rikkei.bank.entity.Transaction;
import com.rikkei.bank.entity.User;
import com.rikkei.bank.exception.BadRequestException;
import com.rikkei.bank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionHistoryService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    public Page<TransactionHistoryResponse> getTransactionHistory(String accountNumber, User currentUser, Pageable pageable) {
        // 1. Kiểm tra tài khoản tồn tại
        Account account = accountService.findByAccountNumber(accountNumber);

        // 2. Kiểm tra quyền sở hữu
        if (!account.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You don't own this account");
        }

        // 3. Lấy lịch sử giao dịch (JPQL với OR condition)
        Page<Transaction> transactions = transactionRepository.findTransactionsByAccount(account, pageable);

        // 4. Chuyển đổi sang DTO và tính toán loại giao dịch
        return transactions.map(tx -> toResponse(tx, accountNumber));
    }

    private TransactionHistoryResponse toResponse(Transaction transaction, String accountNumber) {
        // Xác định loại giao dịch: DEBIT (trừ tiền) hay CREDIT (cộng tiền)
        boolean isDebit = transaction.getFromAccount().getAccountNumber().equals(accountNumber);

        // Xác định tài khoản đối diện
        String counterPartyAccount;
        String counterPartyName;

        if (isDebit) {
            // Nếu là DEBIT, tài khoản đối diện là tài khoản nhận
            counterPartyAccount = transaction.getToAccount().getAccountNumber();
            counterPartyName = transaction.getToAccount().getAccountName();
        } else {
            // Nếu là CREDIT, tài khoản đối diện là tài khoản gửi
            counterPartyAccount = transaction.getFromAccount().getAccountNumber();
            counterPartyName = transaction.getFromAccount().getAccountName();
        }

        // Tính số dư sau giao dịch (không lưu trong DB, tính từ số dư hiện tại)
        BigDecimal balanceAfter = calculateBalanceAfter(transaction, accountNumber);

        return TransactionHistoryResponse.builder()
                .transactionCode(transaction.getTransactionCode())
                .transactionDate(transaction.getCreatedAt())
                .amount(transaction.getAmount())
                .type(isDebit ? "DEBIT" : "CREDIT")
                .counterPartyAccount(counterPartyAccount)
                .counterPartyName(counterPartyName)
                .description(transaction.getDescription())
                .balanceAfter(balanceAfter)
                .transactionType(transaction.getTransactionType().toString())
                .build();
    }

    private BigDecimal calculateBalanceAfter(Transaction transaction, String accountNumber) {
        // Lấy tài khoản hiện tại
        Account currentAccount;
        if (transaction.getFromAccount().getAccountNumber().equals(accountNumber)) {
            currentAccount = transaction.getFromAccount();
        } else {
            currentAccount = transaction.getToAccount();
        }

        // Trả về số dư hiện tại của tài khoản
        return currentAccount.getBalance();
    }
}