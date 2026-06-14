package com.rikkei.bank.service.account;

import com.rikkei.bank.dto.account.response.AccountResponse;
import com.rikkei.bank.entity.User;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

public interface IAccountService {

    AccountResponse openAccount(User user, String accountName);

    Page<AccountResponse> getMyAccounts(User user, int page, int size);

    BigDecimal getBalance(String accountNumber, User user);

    com.rikkei.bank.entity.Account findByAccountNumber(String accountNumber);
}