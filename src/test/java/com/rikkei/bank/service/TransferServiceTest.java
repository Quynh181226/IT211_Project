package com.rikkei.bank.service;

import com.rikkei.bank.dto.transaction.request.TransferRequest;
import com.rikkei.bank.dto.transaction.response.TransferResponse;
import com.rikkei.bank.entity.Account;
import com.rikkei.bank.entity.Transaction;
import com.rikkei.bank.entity.User;
import com.rikkei.bank.exception.BadRequestException;
import com.rikkei.bank.exception.InsufficientBalanceException;
import com.rikkei.bank.repository.AccountRepository;
import com.rikkei.bank.repository.TransactionRepository;
import com.rikkei.bank.service.account.IAccountService;
import com.rikkei.bank.service.transaction.impl.TransferServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransferService Unit Tests")
class TransferServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private IAccountService accountService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TransferServiceImpl transferService;

    private User currentUser;
    private Account fromAccount;
    private Account toAccount;
    private TransferRequest validRequest;

    @BeforeEach
    void setUp() {
        currentUser = User.builder()
                .id(1L)
                .username("customer1")
                .fullName("Nguyen Van A")
                .pin("$2a$10$encodedPin123456")
                .isKyc(true)
                .isLocked(false)
                .build();

        fromAccount = Account.builder()
                .id(1L)
                .accountNumber("890202506101430520123")
                .accountName("Tai khoan thanh toan")
                .balance(BigDecimal.valueOf(10000000))
                .isActive(true)
                .user(currentUser)
                .build();

        User toUser = User.builder()
                .id(2L)
                .username("customer2")
                .fullName("Nguyen Van B")
                .build();

        toAccount = Account.builder()
                .id(2L)
                .accountNumber("890202506101430520124")
                .accountName("Tai khoan nhan")
                .balance(BigDecimal.valueOf(5000000))
                .isActive(true)
                .user(toUser)
                .build();

        validRequest = TransferRequest.builder()
                .fromAccountNumber("890202506101430520123")
                .toAccountNumber("890202506101430520124")
                .amount(BigDecimal.valueOf(2000000))
                .description("Chuyen tien tra no")
                .pin("123456")
                .build();
    }

    // ==================== TEST CHUYỂN TIỀN THÀNH CÔNG ====================
    @Test
    @DisplayName("Should transfer successfully when all conditions are met")
    void testTransferSuccess_InternalTransfer() {
        when(accountService.findByAccountNumber(validRequest.getFromAccountNumber()))
                .thenReturn(fromAccount);
        when(accountService.findByAccountNumber(validRequest.getToAccountNumber()))
                .thenReturn(toAccount);
        when(passwordEncoder.matches(validRequest.getPin(), currentUser.getPin()))
                .thenReturn(true);
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransferResponse response = transferService.transfer(validRequest, currentUser);

        assertThat(response).isNotNull();
        assertThat(response.getTransactionCode()).isNotNull();
        assertThat(response.getFromAccountNumber()).isEqualTo(fromAccount.getAccountNumber());
        assertThat(response.getToAccountNumber()).isEqualTo(toAccount.getAccountNumber());
        assertThat(response.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(2000000));
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getRemainingBalance()).isEqualByComparingTo(BigDecimal.valueOf(8000000));

        assertThat(fromAccount.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(8000000));
        assertThat(toAccount.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(7000000));

        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    // ==================== TEST CHUYỂN TIỀN LIÊN NGÂN HÀNG ====================
    @Test
    @DisplayName("Should transfer to external bank successfully")
    void testTransferSuccess_ExternalTransfer() {
        TransferRequest externalRequest = TransferRequest.builder()
                .fromAccountNumber("890202506101430520123")
                .toAccountNumber("1234567890")
                .amount(BigDecimal.valueOf(1000000))
                .description("Chuyen lien ngan hang")
                .toBankName("Vietcombank")
                .pin("123456")
                .build();

        Account externalAccount = Account.builder()
                .id(3L)
                .accountNumber("1234567890")
                .accountName("External Account")
                .balance(BigDecimal.ZERO)
                .bankName("Vietcombank")
                .isActive(true)
                .user(null)
                .build();

        when(accountService.findByAccountNumber(externalRequest.getFromAccountNumber()))
                .thenReturn(fromAccount);
        when(accountRepository.findByAccountNumber(externalRequest.getToAccountNumber()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(externalAccount));
        when(passwordEncoder.matches(externalRequest.getPin(), currentUser.getPin()))
                .thenReturn(true);
        when(accountRepository.save(any(Account.class)))
                .thenReturn(externalAccount);
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransferResponse response = transferService.transfer(externalRequest, currentUser);

        assertThat(response).isNotNull();
        assertThat(response.getToBankName()).isEqualTo("Vietcombank");
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(fromAccount.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(9000000));
    }

    // ==================== TEST KHI KHÔNG ĐỦ SỐ DƯ ====================
    @Test
    @DisplayName("Should throw exception when balance is insufficient")
    void testTransfer_InsufficientBalance() {
        TransferRequest request = TransferRequest.builder()
                .fromAccountNumber("890202506101430520123")
                .toAccountNumber("890202506101430520124")
                .amount(BigDecimal.valueOf(20000000))
                .description("Chuyen tien")
                .pin("123456")
                .build();

        when(accountService.findByAccountNumber(request.getFromAccountNumber()))
                .thenReturn(fromAccount);
        when(passwordEncoder.matches(request.getPin(), currentUser.getPin()))
                .thenReturn(true);

        assertThatThrownBy(() -> transferService.transfer(request, currentUser))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("Insufficient balance");

        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

    // ==================== TEST KHI TÀI KHOẢN NGUỒN KHÔNG TỒN TẠI ====================
    @Test
    @DisplayName("Should throw exception when source account not found")
    void testTransfer_SourceAccountNotFound() {
        when(accountService.findByAccountNumber(validRequest.getFromAccountNumber()))
                .thenThrow(new BadRequestException("Account not found: " + validRequest.getFromAccountNumber()));

        assertThatThrownBy(() -> transferService.transfer(validRequest, currentUser))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Account not found");
    }

    // ==================== TEST KHI TÀI KHOẢN NGUỒN KHÔNG PHẢI CỦA USER ====================
    @Test
    @DisplayName("Should throw exception when user doesn't own source account")
    void testTransfer_NotOwnSourceAccount() {
        User otherUser = User.builder().id(99L).username("other").build();
        Account otherAccount = Account.builder()
                .id(99L)
                .accountNumber("890999")
                .balance(BigDecimal.valueOf(10000000))
                .user(otherUser)
                .build();

        when(accountService.findByAccountNumber(validRequest.getFromAccountNumber()))
                .thenReturn(otherAccount);

        assertThatThrownBy(() -> transferService.transfer(validRequest, currentUser))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("You don't own this account");
    }

    // ==================== TEST KHI TÀI KHOẢN NGUỒN BỊ KHÓA ====================
    @Test
    @DisplayName("Should throw exception when source account is inactive")
    void testTransfer_SourceAccountInactive() {
        fromAccount.setActive(false);

        when(accountService.findByAccountNumber(validRequest.getFromAccountNumber()))
                .thenReturn(fromAccount);

        assertThatThrownBy(() -> transferService.transfer(validRequest, currentUser))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Source account is inactive");
    }

    // ==================== TEST KHI PIN SAI ====================
    @Test
    @DisplayName("Should throw exception when PIN is incorrect")
    void testTransfer_WrongPin() {
        when(accountService.findByAccountNumber(validRequest.getFromAccountNumber()))
                .thenReturn(fromAccount);
        when(passwordEncoder.matches(validRequest.getPin(), currentUser.getPin()))
                .thenReturn(false);

        assertThatThrownBy(() -> transferService.transfer(validRequest, currentUser))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid transaction PIN");
    }

    // ==================== TEST KHI CHUYỂN TIỀN CHO CHÍNH MÌNH ====================
    @Test
    @DisplayName("Should throw exception when transferring to same account")
    void testTransfer_ToSameAccount() {
        TransferRequest sameAccountRequest = TransferRequest.builder()
                .fromAccountNumber("890202506101430520123")
                .toAccountNumber("890202506101430520123")
                .amount(BigDecimal.valueOf(1000000))
                .pin("123456")
                .build();

        when(accountService.findByAccountNumber(sameAccountRequest.getFromAccountNumber()))
                .thenReturn(fromAccount);
        when(accountService.findByAccountNumber(sameAccountRequest.getToAccountNumber()))
                .thenReturn(fromAccount);
        when(passwordEncoder.matches(sameAccountRequest.getPin(), currentUser.getPin()))
                .thenReturn(true);

        assertThatThrownBy(() -> transferService.transfer(sameAccountRequest, currentUser))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot transfer to the same account");
    }

    // ==================== TEST KHI SỐ TIỀN LÀ SỐ ÂM HOẶC 0 ====================
    @Test
    @DisplayName("Should throw exception when amount is zero or negative")
    void testTransfer_InvalidAmount() {
        TransferRequest zeroAmountRequest = TransferRequest.builder()
                .fromAccountNumber("890202506101430520123")
                .toAccountNumber("890202506101430520124")
                .amount(BigDecimal.ZERO)
                .pin("123456")
                .build();

        assertThat(zeroAmountRequest.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}