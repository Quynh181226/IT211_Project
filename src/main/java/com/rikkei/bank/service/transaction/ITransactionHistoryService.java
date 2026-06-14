package com.rikkei.bank.service.transaction;

import com.rikkei.bank.dto.transaction.response.TransactionHistoryResponse;
import com.rikkei.bank.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ITransactionHistoryService {

    Page<TransactionHistoryResponse> getTransactionHistory(String accountNumber, User currentUser, Pageable pageable);
}