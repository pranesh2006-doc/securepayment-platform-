package com.securepay.securepayment.repository;

import com.securepay.securepayment.entity.IntegritySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IntegritySnapshotRepository
        extends JpaRepository<IntegritySnapshot, Long> {

    Optional<IntegritySnapshot> findByTransactionId(
            String transactionId
    );
}