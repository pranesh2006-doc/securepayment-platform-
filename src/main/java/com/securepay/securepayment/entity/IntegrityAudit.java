package com.securepay.securepayment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "integrity_audits")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntegrityAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "transaction_id",
            nullable = false,
            length = 30
    )
    private String transactionId;

    @Column(
            name = "verification_time",
            nullable = false
    )
    private LocalDateTime verificationTime;

    @Column(
            nullable = false,
            length = 30
    )
    private String result;

    @Column(
            name = "original_hash",
            length = 64
    )
    private String originalHash;

    @Column(
            name = "current_hash",
            length = 64
    )
    private String currentHash;

    @Column(
            name = "changed_fields",
            columnDefinition = "TEXT"
    )
    private String changedFields;
}