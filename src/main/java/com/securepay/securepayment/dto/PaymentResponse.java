package com.securepay.securepayment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private String transactionId;

    private String sender;

    private String receiver;

    private BigDecimal amount;

    private String status;

    private LocalDateTime timestamp;
}