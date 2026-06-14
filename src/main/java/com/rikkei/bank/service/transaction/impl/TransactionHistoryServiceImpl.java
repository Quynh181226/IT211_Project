package com.rikkei.bank.service.transaction.impl;

import com.rikkei.bank.dto.transaction.response.TransactionHistoryResponse;
import com.rikkei.bank.entity.Account;
import com.rikkei.bank.entity.Transaction;
import com.rikkei.bank.entity.User;
import com.rikkei.bank.exception.BadRequestException;
import com.rikkei.bank.repository.TransactionRepository;
import com.rikkei.bank.service.account.IAccountService;
import com.rikkei.bank.service.transaction.ITransactionHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionHistoryServiceImpl implements ITransactionHistoryService {

    private final TransactionRepository transactionRepository;
    private final IAccountService accountService;

    @Override
    public Page<TransactionHistoryResponse> getTransactionHistory(String accountNumber, User currentUser, Pageable pageable) {
        Account account = accountService.findByAccountNumber(accountNumber);

        if (!account.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You don't own this account");
        }

        Page<Transaction> transactions = transactionRepository.findTransactionsByAccount(account, pageable);

        return transactions.map(tx -> toResponse(tx, accountNumber));
    }

    private TransactionHistoryResponse toResponse(Transaction transaction, String accountNumber) {
        boolean isDebit = transaction.getFromAccount().getAccountNumber().equals(accountNumber);

        String counterPartyAccount;
        String counterPartyName;

        if (isDebit) {
            counterPartyAccount = transaction.getToAccount().getAccountNumber();
            counterPartyName = transaction.getToAccount().getAccountName();
        } else {
            counterPartyAccount = transaction.getFromAccount().getAccountNumber();
            counterPartyName = transaction.getFromAccount().getAccountName();
        }

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
        Account currentAccount;
        if (transaction.getFromAccount().getAccountNumber().equals(accountNumber)) {
            currentAccount = transaction.getFromAccount();
        } else {
            currentAccount = transaction.getToAccount();
        }

        return currentAccount.getBalance();
    }
}