package com.securepay.securepayment.service;

import com.securepay.securepayment.entity.IntegrityAudit;
import com.securepay.securepayment.entity.IntegritySnapshot;
import com.securepay.securepayment.entity.Transaction;
import com.securepay.securepayment.repository.IntegrityAuditRepository;
import com.securepay.securepayment.repository.IntegritySnapshotRepository;
import com.securepay.securepayment.repository.TransactionRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class IntegrityService {

    private final TransactionRepository transactionRepository;
    private final IntegritySnapshotRepository integritySnapshotRepository;
    private final IntegrityAuditRepository integrityAuditRepository;

    public IntegrityService(
            TransactionRepository transactionRepository,
            IntegritySnapshotRepository integritySnapshotRepository,
            IntegrityAuditRepository integrityAuditRepository) {

        this.transactionRepository = transactionRepository;
        this.integritySnapshotRepository = integritySnapshotRepository;
        this.integrityAuditRepository = integrityAuditRepository;
    }

    // =====================================================
    // GENERATE HASH
    // =====================================================

    public String generateHash(Transaction transaction) {

        String data =
                transaction.getTransactionId() + "|" +
                        transaction.getSender() + "|" +
                        transaction.getReceiver() + "|" +
                        formatAmount(transaction.getAmount()) + "|" +
                        transaction.getStatus() + "|" +
                        formatTimestamp(transaction.getTimestamp());

        return sha256(data);
    }

    // =====================================================
    // VERIFY TRANSACTION
    // =====================================================

    public String verifyTransaction(String transactionId) {

        // -------------------------------------------------
        // Find current transaction
        // -------------------------------------------------

        Transaction transaction =
                transactionRepository
                        .findByTransactionId(transactionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Transaction not found"
                                )
                        );

        // -------------------------------------------------
        // Generate current hash
        // -------------------------------------------------

        String currentHash =
                generateHash(transaction);

        // -------------------------------------------------
        // Get original hash
        // -------------------------------------------------

        String originalHash =
                transaction.getIntegrityHash();

        // -------------------------------------------------
        // Find original snapshot
        // -------------------------------------------------

        IntegritySnapshot snapshot =
                integritySnapshotRepository
                        .findByTransactionId(transactionId)
                        .orElse(null);

        // -------------------------------------------------
        // Compare hashes
        // -------------------------------------------------

        boolean hashMatches =
                originalHash != null &&
                        originalHash.equals(currentHash);

        String result;

        if (hashMatches) {

            result = "NO TAMPER";

        } else {

            result = "TAMPER DETECTED";
        }

        // -------------------------------------------------
        // Find changed fields
        // -------------------------------------------------

        String changedFields =
                findChangedFields(
                        snapshot,
                        transaction
                );

        // -------------------------------------------------
        // Create audit record
        // -------------------------------------------------

        IntegrityAudit audit =
                new IntegrityAudit();

        audit.setTransactionId(
                transactionId
        );

        audit.setVerificationTime(
                LocalDateTime.now()
        );

        audit.setResult(
                result
        );

        audit.setOriginalHash(
                originalHash
        );

        audit.setCurrentHash(
                currentHash
        );

        audit.setChangedFields(
                changedFields
        );

        integrityAuditRepository.save(audit);

        // -------------------------------------------------
        // Console output
        // -------------------------------------------------

        System.out.println("=================================");
        System.out.println("INTEGRITY VERIFICATION");
        System.out.println("TRANSACTION ID : " + transactionId);
        System.out.println("ORIGINAL HASH  : " + originalHash);
        System.out.println("CURRENT HASH   : " + currentHash);
        System.out.println("RESULT         : " + result);
        System.out.println("CHANGED FIELDS : ");
        System.out.println(changedFields);
        System.out.println("=================================");

        return result;
    }

    // =====================================================
    // GET AUDIT HISTORY
    // =====================================================

    public List<IntegrityAudit> getAuditHistory(
            String transactionId) {

        return integrityAuditRepository
                .findByTransactionIdOrderByVerificationTimeDesc(
                        transactionId
                );
    }

    // =====================================================
    // FIND CHANGED FIELDS
    // =====================================================

    private String findChangedFields(
            IntegritySnapshot snapshot,
            Transaction current) {

        // -------------------------------------------------
        // Snapshot doesn't exist
        // -------------------------------------------------

        if (snapshot == null) {

            return "Original integrity snapshot not found";
        }

        StringBuilder changes =
                new StringBuilder();

        // -------------------------------------------------
        // Sender
        // -------------------------------------------------

        if (!safeEquals(
                snapshot.getSender(),
                current.getSender())) {

            changes.append(
                    "Sender: " +
                            snapshot.getSender() +
                            " -> " +
                            current.getSender() +
                            "\n"
            );
        }

        // -------------------------------------------------
        // Receiver
        // -------------------------------------------------

        if (!safeEquals(
                snapshot.getReceiver(),
                current.getReceiver())) {

            changes.append(
                    "Receiver: " +
                            snapshot.getReceiver() +
                            " -> " +
                            current.getReceiver() +
                            "\n"
            );
        }

        // -------------------------------------------------
        // Amount
        // -------------------------------------------------

        if (!amountEquals(
                snapshot.getAmount(),
                current.getAmount())) {

            changes.append(
                    "Amount: ₹" +
                            formatAmount(snapshot.getAmount()) +
                            " -> ₹" +
                            formatAmount(current.getAmount()) +
                            "\n"
            );
        }

        // -------------------------------------------------
        // Status
        // -------------------------------------------------

        if (!safeEquals(
                snapshot.getStatus(),
                current.getStatus())) {

            changes.append(
                    "Status: " +
                            snapshot.getStatus() +
                            " -> " +
                            current.getStatus() +
                            "\n"
            );
        }

        // -------------------------------------------------
        // Timestamp
        // -------------------------------------------------

        if (!safeEquals(
                formatTimestamp(snapshot.getTimestamp()),
                formatTimestamp(current.getTimestamp()))) {

            changes.append(
                    "Timestamp: " +
                            formatTimestamp(snapshot.getTimestamp()) +
                            " -> " +
                            formatTimestamp(current.getTimestamp()) +
                            "\n"
            );
        }

        // -------------------------------------------------
        // Nothing changed
        // -------------------------------------------------

        if (changes.length() == 0) {

            return "None";
        }

        return changes.toString().trim();
    }

    // =====================================================
    // SAFE STRING COMPARISON
    // =====================================================

    private boolean safeEquals(
            Object first,
            Object second) {

        if (first == null && second == null) {
            return true;
        }

        if (first == null || second == null) {
            return false;
        }

        return first.toString()
                .equals(second.toString());
    }

    // =====================================================
    // BIG DECIMAL COMPARISON
    // =====================================================

    private boolean amountEquals(
            BigDecimal first,
            BigDecimal second) {

        if (first == null && second == null) {
            return true;
        }

        if (first == null || second == null) {
            return false;
        }

        return first.compareTo(second) == 0;
    }

    // =====================================================
    // SHA-256
    // =====================================================

    private String sha256(String data) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hashBytes =
                    digest.digest(
                            data.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            StringBuilder result =
                    new StringBuilder();

            for (byte b : hashBytes) {

                result.append(
                        String.format(
                                "%02x",
                                b
                        )
                );
            }

            return result.toString();

        } catch (Exception e) {

            throw new RuntimeException(
                    "SHA-256 generation failed",
                    e
            );
        }
    }

    // =====================================================
    // AMOUNT FORMAT
    // =====================================================

    private String formatAmount(
            BigDecimal amount) {

        return amount == null
                ? ""
                : amount
                .setScale(2)
                .toPlainString();
    }

    // =====================================================
    // TIMESTAMP FORMAT
    // =====================================================

    private String formatTimestamp(
            LocalDateTime timestamp) {

        return timestamp == null
                ? ""
                : timestamp
                .withNano(
                        (timestamp.getNano() / 1_000_000)
                                * 1_000_000
                )
                .toString();
    }}
