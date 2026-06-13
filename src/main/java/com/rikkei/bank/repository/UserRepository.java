package com.rikkei.bank.repository;

import com.rikkei.bank.dto.response.UserResponse;
import com.rikkei.bank.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("SELECT NEW com.rikkei.bank.dto.response.UserResponse(" +
            "u.id, u.fullName, u.username, u.isKyc, u.isLocked) " +
            "FROM User u")
    Page<UserResponse> findAllUserProjection(Pageable pageable);
}