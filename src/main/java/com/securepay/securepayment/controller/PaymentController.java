package com.securepay.securepayment.controller;

import com.securepay.securepayment.dto.PaymentRequest;
import com.securepay.securepayment.dto.PaymentResponse;
import com.securepay.securepayment.entity.Transaction;
import com.securepay.securepayment.service.PaymentService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(
            PaymentService paymentService) {

        this.paymentService =
                paymentService;
    }


    // =====================================================
    // TEST USER
    // =====================================================

    @GetMapping("/test-user")
    public String testUser(
            Authentication authentication) {

        return authentication.getName();
    }


    // =====================================================
    // CREATE PAYMENT
    // POST /payment
    // =====================================================

    @PostMapping
    public ResponseEntity<PaymentResponse> makePayment(
            @Valid @RequestBody PaymentRequest request,
            Authentication authentication) {

        String sender =
                authentication.getName();

        PaymentResponse response =
                paymentService.makePayment(
                        sender,
                        request
                );

        return ResponseEntity.ok(
                response
        );
    }


    // =====================================================
    // TRANSACTION HISTORY
    // GET /payment/history
    // =====================================================

    @GetMapping("/history")
    public ResponseEntity<List<Transaction>>
    getHistory(
            Authentication authentication) {

        String sender =
                authentication.getName();

        List<Transaction> transactions =
                paymentService
                        .getTransactionHistory(
                                sender
                        );

        return ResponseEntity.ok(
                transactions
        );
    }


    // =====================================================
    // SPECIFIC TRANSACTION
    // GET /payment/{transactionId}
    // =====================================================

    @GetMapping("/{transactionId}")
    public ResponseEntity<Transaction>
    getTransaction(
            @PathVariable String transactionId,
            Authentication authentication) {

        String sender =
                authentication.getName();

        Transaction transaction =
                paymentService.getSpecificTransaction(
                        transactionId,
                        sender
                );

        return ResponseEntity.ok(
                transaction
        );
    }
}