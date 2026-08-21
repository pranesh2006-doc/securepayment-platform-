package com.securepay.securepayment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class PaymentRequest {

    @NotBlank(message = "Receiver is required")
    private String receiver;

    @NotNull(message = "Amount is required")
    @DecimalMin(
            value = "1.00",
            message = "Amount must be greater than zero"
    )
    private BigDecimal amount;

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}