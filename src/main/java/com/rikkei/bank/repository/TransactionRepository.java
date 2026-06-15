package com.rikkei.bank.repository;

import com.rikkei.bank.entity.Account;
import com.rikkei.bank.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("SELECT t FROM Transaction t " +
            "JOIN FETCH t.fromAccount " +
            "JOIN FETCH t.toAccount " +
            "WHERE (t.fromAccount = :account OR t.toAccount = :account) " +
            "AND t.status = 'SUCCESS' " +
            "ORDER BY t.createdAt DESC")
    Page<Transaction> findTransactionsByAccount(@Param("account") Account account, Pageable pageable);
}