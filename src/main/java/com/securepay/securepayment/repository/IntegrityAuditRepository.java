package com.securepay.securepayment.repository;

import com.securepay.securepayment.entity.IntegrityAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IntegrityAuditRepository
        extends JpaRepository<IntegrityAudit, Long> {

    List<IntegrityAudit> findByTransactionIdOrderByVerificationTimeDesc(
            String transactionId
    );
}