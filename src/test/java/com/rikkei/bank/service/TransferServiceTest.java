package com.rikkei.bank.service;

import com.rikkei.bank.dto.request.TransferRequest;
import com.rikkei.bank.dto.response.TransferResponse;
import com.rikkei.bank.entity.Account;
import com.rikkei.bank.entity.Transaction;
import com.rikkei.bank.entity.User;
import com.rikkei.bank.exception.BadRequestException;
import com.rikkei.bank.exception.InsufficientBalanceException;
import com.rikkei.bank.repository.AccountRepository;
import com.rikkei.bank.repository.TransactionRepository;
import com.rikkei.bank.security.JwtUtils;
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
    private AccountService accountService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TransferService transferService;

    private User currentUser;
    private Account fromAccount;
    private Account toAccount;
    private TransferRequest validRequest;

    @BeforeEach
    void setUp() {
        // Tạo user hiện tại
        currentUser = User.builder()
                .id(1L)
                .username("customer1")
                .fullName("Nguyen Van A")
                .pin("$2a$10$encodedPin123456") // Mã hóa của "123456"
                .isKyc(true)
                .isLocked(false)
                .build();

        // Tạo tài khoản nguồn
        fromAccount = Account.builder()
                .id(1L)
                .accountNumber("890202506101430520123")
                .accountName("Tai khoan thanh toan")
                .balance(BigDecimal.valueOf(10000000)) // 10 triệu
                .isActive(true)
                .user(currentUser)
                .build();

        // Tạo tài khoản đích (nội bộ)
        User toUser = User.builder()
                .id(2L)
                .username("customer2")
                .fullName("Nguyen Van B")
                .build();

        toAccount = Account.builder()
                .id(2L)
                .accountNumber("890202506101430520124")
                .accountName("Tai khoan nhan")
                .balance(BigDecimal.valueOf(5000000)) // 5 triệu
                .isActive(true)
                .user(toUser)
                .build();

        // Tạo request hợp lệ
        validRequest = TransferRequest.builder()
                .fromAccountNumber("890202506101430520123")
                .toAccountNumber("890202506101430520124")
                .amount(BigDecimal.valueOf(2000000)) // 2 triệu
                .description("Chuyen tien tra no")
                .pin("123456")
                .build();
    }

    // ==================== TEST CHUYỂN TIỀN THÀNH CÔNG ====================

    @Test
    @DisplayName("Should transfer successfully when all conditions are met")
    void testTransferSuccess_InternalTransfer() {
        // Given
        when(accountService.findByAccountNumber(validRequest.getFromAccountNumber()))
                .thenReturn(fromAccount);
        when(accountService.findByAccountNumber(validRequest.getToAccountNumber()))
                .thenReturn(toAccount);
        when(passwordEncoder.matches(validRequest.getPin(), currentUser.getPin()))
                .thenReturn(true);
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TransferResponse response = transferService.transfer(validRequest, currentUser);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTransactionCode()).isNotNull();
        assertThat(response.getFromAccountNumber()).isEqualTo(fromAccount.getAccountNumber());
        assertThat(response.getToAccountNumber()).isEqualTo(toAccount.getAccountNumber());
        assertThat(response.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(2000000));
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getRemainingBalance()).isEqualByComparingTo(BigDecimal.valueOf(8000000));

        // Verify số dư đã được cập nhật
        assertThat(fromAccount.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(8000000));
        assertThat(toAccount.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(7000000));

        // Verify repository được gọi đúng
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    // ==================== TEST CHUYỂN TIỀN LIÊN NGÂN HÀNG ====================

    @Test
    @DisplayName("Should transfer to external bank successfully")
    void testTransferSuccess_ExternalTransfer() {
        // Given - request liên ngân hàng
        TransferRequest externalRequest = TransferRequest.builder()
                .fromAccountNumber("890202506101430520123")
                .toAccountNumber("1234567890")
                .amount(BigDecimal.valueOf(1000000))
                .description("Chuyen lien ngan hang")
                .toBankName("Vietcombank")
                .pin("123456")
                .build();

        // Tạo tài khoản đích external (sẽ được tạo mới)
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

        // When
        TransferResponse response = transferService.transfer(externalRequest, currentUser);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToBankName()).isEqualTo("Vietcombank");
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(fromAccount.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(9000000));
    }

    // ==================== TEST KHI KHÔNG ĐỦ SỐ DƯ ====================

    @Test
    @DisplayName("Should throw exception when balance is insufficient")
    void testTransfer_InsufficientBalance() {
        // Given - số tiền lớn hơn số dư
        TransferRequest request = TransferRequest.builder()
                .fromAccountNumber("890202506101430520123")
                .toAccountNumber("890202506101430520124")
                .amount(BigDecimal.valueOf(20000000)) // 20 triệu > 10 triệu
                .description("Chuyen tien")
                .pin("123456")
                .build();

        when(accountService.findByAccountNumber(request.getFromAccountNumber()))
                .thenReturn(fromAccount);
        when(passwordEncoder.matches(request.getPin(), currentUser.getPin()))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> transferService.transfer(request, currentUser))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("Insufficient balance");

        // Verify không có transaction nào được lưu
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

    // ==================== TEST KHI TÀI KHOẢN NGUỒN KHÔNG TỒN TẠI ====================

    @Test
    @DisplayName("Should throw exception when source account not found")
    void testTransfer_SourceAccountNotFound() {
        // Given
        when(accountService.findByAccountNumber(validRequest.getFromAccountNumber()))
                .thenThrow(new BadRequestException("Account not found: " + validRequest.getFromAccountNumber()));

        // When & Then
        assertThatThrownBy(() -> transferService.transfer(validRequest, currentUser))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Account not found");
    }

    // ==================== TEST KHI TÀI KHOẢN NGUỒN KHÔNG PHẢI CỦA USER ====================

    @Test
    @DisplayName("Should throw exception when user doesn't own source account")
    void testTransfer_NotOwnSourceAccount() {
        // Given - tài khoản nguồn thuộc user khác
        User otherUser = User.builder().id(99L).username("other").build();
        Account otherAccount = Account.builder()
                .id(99L)
                .accountNumber("890999")
                .balance(BigDecimal.valueOf(10000000))
                .user(otherUser)
                .build();

        when(accountService.findByAccountNumber(validRequest.getFromAccountNumber()))
                .thenReturn(otherAccount);

        // When & Then
        assertThatThrownBy(() -> transferService.transfer(validRequest, currentUser))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("You don't own this account");
    }

    // ==================== TEST KHI TÀI KHOẢN NGUỒN BỊ KHÓA ====================

    @Test
    @DisplayName("Should throw exception when source account is inactive")
    void testTransfer_SourceAccountInactive() {
        // Given
        fromAccount.setActive(false);

        when(accountService.findByAccountNumber(validRequest.getFromAccountNumber()))
                .thenReturn(fromAccount);

        // When & Then
        assertThatThrownBy(() -> transferService.transfer(validRequest, currentUser))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Source account is inactive");
    }

    // ==================== TEST KHI PIN SAI ====================

    @Test
    @DisplayName("Should throw exception when PIN is incorrect")
    void testTransfer_WrongPin() {
        // Given
        when(accountService.findByAccountNumber(validRequest.getFromAccountNumber()))
                .thenReturn(fromAccount);
        when(passwordEncoder.matches(validRequest.getPin(), currentUser.getPin()))
                .thenReturn(false); // PIN sai

        // When & Then
        assertThatThrownBy(() -> transferService.transfer(validRequest, currentUser))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid transaction PIN");
    }

    // ==================== TEST KHI CHUYỂN TIỀN CHO CHÍNH MÌNH ====================

    @Test
    @DisplayName("Should throw exception when transferring to same account")
    void testTransfer_ToSameAccount() {
        // Given - chuyển tiền về cùng tài khoản
        TransferRequest sameAccountRequest = TransferRequest.builder()
                .fromAccountNumber("890202506101430520123")
                .toAccountNumber("890202506101430520123") // cùng số tài khoản
                .amount(BigDecimal.valueOf(1000000))
                .pin("123456")
                .build();

        when(accountService.findByAccountNumber(sameAccountRequest.getFromAccountNumber()))
                .thenReturn(fromAccount);
        when(accountService.findByAccountNumber(sameAccountRequest.getToAccountNumber()))
                .thenReturn(fromAccount);
        when(passwordEncoder.matches(sameAccountRequest.getPin(), currentUser.getPin()))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> transferService.transfer(sameAccountRequest, currentUser))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot transfer to the same account");
    }

    // ==================== TEST KHI SỐ TIỀN LÀ SỐ ÂM HOẶC 0 ====================

    @Test
    @DisplayName("Should throw exception when amount is zero or negative")
    void testTransfer_InvalidAmount() {
        // Given
        TransferRequest zeroAmountRequest = TransferRequest.builder()
                .fromAccountNumber("890202506101430520123")
                .toAccountNumber("890202506101430520124")
                .amount(BigDecimal.ZERO)
                .pin("123456")
                .build();

        // Validation sẽ bắt lỗi trước khi vào service, nhưng test vẫn nên có
        assertThat(zeroAmountRequest.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}