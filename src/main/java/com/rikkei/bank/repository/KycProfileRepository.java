package com.rikkei.bank.repository;

import com.rikkei.bank.constants.KycStatus;
import com.rikkei.bank.entity.KycProfile;
import com.rikkei.bank.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KycProfileRepository extends JpaRepository<KycProfile, Long> {

    Optional<KycProfile> findByUser(User user);

    Page<KycProfile> findByStatus(KycStatus status, Pageable pageable);

    boolean existsByUserAndStatus(User user, KycStatus status);
}