package com.securepay.securepayment.service;

import com.securepay.securepayment.dto.PaymentRequest;
import com.securepay.securepayment.dto.PaymentResponse;
import com.securepay.securepayment.entity.IntegritySnapshot;
import com.securepay.securepayment.entity.Transaction;
import com.securepay.securepayment.repository.IntegritySnapshotRepository;
import com.securepay.securepayment.repository.TransactionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final IntegrityService integrityService;
    private final IntegritySnapshotRepository integritySnapshotRepository;

    public PaymentService(
            TransactionRepository transactionRepository,
            IntegrityService integrityService,
            IntegritySnapshotRepository integritySnapshotRepository) {

        this.transactionRepository = transactionRepository;
        this.integrityService = integrityService;
        this.integritySnapshotRepository = integritySnapshotRepository;
    }

    // =====================================================
    // CREATE PAYMENT
    // =====================================================

    @Transactional
    public PaymentResponse makePayment(
            String sender,
            PaymentRequest request) {

        // -------------------------------------------------
        // Validate sender
        // -------------------------------------------------

        if (sender == null || sender.trim().isEmpty()) {
            throw new RuntimeException("Sender is required");
        }

        // -------------------------------------------------
        // Validate request
        // -------------------------------------------------

        if (request == null) {
            throw new RuntimeException(
                    "Payment request cannot be null"
            );
        }

        // -------------------------------------------------
        // Validate receiver
        // -------------------------------------------------

        if (request.getReceiver() == null ||
                request.getReceiver().trim().isEmpty()) {

            throw new RuntimeException(
                    "Receiver is required"
            );
        }

        // -------------------------------------------------
        // Validate amount
        // -------------------------------------------------

        if (request.getAmount() == null ||
                request.getAmount()
                        .compareTo(java.math.BigDecimal.ZERO) <= 0) {

            throw new RuntimeException(
                    "Amount must be greater than zero"
            );
        }

        // =================================================
        // GENERATE TRANSACTION ID
        // =================================================

        String transactionId =
                "TX" +
                        UUID.randomUUID()
                                .toString()
                                .replace("-", "")
                                .substring(0, 10)
                                .toUpperCase();

        // =================================================
        // CREATE TRANSACTION OBJECT
        // =================================================

        Transaction transaction = new Transaction();

        transaction.setTransactionId(transactionId);

        transaction.setSender(sender);

        transaction.setReceiver(
                request.getReceiver().trim()
        );

        transaction.setAmount(
                request.getAmount()
        );

        transaction.setStatus("SUCCESS");

        // Timestamp is part of the original hash
        transaction.setTimestamp(
                LocalDateTime.now()
        );

        // =================================================
        // GENERATE ORIGINAL SHA-256 HASH
        // =================================================

        String originalHash =
                integrityService.generateHash(transaction);

        // =================================================
        // STORE ORIGINAL HASH
        // =================================================

        transaction.setIntegrityHash(originalHash);

        // =================================================
        // SAVE TRANSACTION
        // =================================================

        Transaction saved =
                transactionRepository.save(transaction);

        // =================================================
        // CREATE INTEGRITY SNAPSHOT
        // =================================================

        IntegritySnapshot snapshot =
                new IntegritySnapshot();

        snapshot.setTransactionId(
                saved.getTransactionId()
        );

        snapshot.setSender(
                saved.getSender()
        );

        snapshot.setReceiver(
                saved.getReceiver()
        );

        snapshot.setAmount(
                saved.getAmount()
        );

        snapshot.setStatus(
                saved.getStatus()
        );

        snapshot.setTimestamp(
                saved.getTimestamp()
        );

        snapshot.setOriginalHash(
                saved.getIntegrityHash()
        );

        integritySnapshotRepository.save(snapshot);

        // =================================================
        // RETURN RESPONSE
        // =================================================

        return new PaymentResponse(
                saved.getTransactionId(),
                saved.getSender(),
                saved.getReceiver(),
                saved.getAmount(),
                saved.getStatus(),
                saved.getTimestamp()
        );
    }

    // =====================================================
    // TRANSACTION HISTORY
    // =====================================================

    public List<Transaction> getTransactionHistory(
            String sender) {

        if (sender == null || sender.trim().isEmpty()) {
            throw new RuntimeException("Sender is required");
        }

        return transactionRepository
                .findBySenderOrderByTimestampDesc(sender);
    }

    // =====================================================
    // SPECIFIC TRANSACTION
    // =====================================================

    public Transaction getSpecificTransaction(
            String transactionId,
            String sender) {

        if (transactionId == null ||
                transactionId.trim().isEmpty()) {

            throw new RuntimeException(
                    "Transaction ID is required"
            );
        }

        if (sender == null ||
                sender.trim().isEmpty()) {

            throw new RuntimeException(
                    "Sender is required"
            );
        }

        Transaction transaction =
                transactionRepository
                        .findByTransactionId(transactionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Transaction not found"
                                )
                        );

        // -------------------------------------------------
        // SECURITY CHECK
        // -------------------------------------------------

        if (!transaction
                .getSender()
                .equals(sender)) {

            throw new RuntimeException(
                    "You are not authorized to view this transaction"
            );
        }

        return transaction;
    }
}