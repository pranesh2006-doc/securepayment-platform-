package com.securepay.securepayment.repository;

import com.securepay.securepayment.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    List<Transaction> findBySenderOrderByTimestampDesc(
            String sender
    );

    Optional<Transaction> findByTransactionId(
            String transactionId
    );
}