package com.rikkei.bank.repository;

import com.rikkei.bank.entity.Account;
import com.rikkei.bank.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);

    Page<Account> findByUser(User user, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Account a SET a.balance = :newBalance, a.version = a.version + 1 WHERE a.id = :id AND a.version = :version")
    int updateBalanceAndVersion(@Param("id") Long id,
                                @Param("newBalance") BigDecimal newBalance,
                                @Param("version") Long version);
}