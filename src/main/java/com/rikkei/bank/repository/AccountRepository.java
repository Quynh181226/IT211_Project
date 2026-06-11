package com.rikkei.bank.repository;

import com.rikkei.bank.entity.Account;
import com.rikkei.bank.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    Page<Account> findByUser(User user, Pageable pageable);

    @Query("SELECT a FROM Account a WHERE a.user.id = :userId AND a.isActive = true")
    Page<Account> findActiveAccountsByUserId(@Param("userId") Long userId, Pageable pageable);
}