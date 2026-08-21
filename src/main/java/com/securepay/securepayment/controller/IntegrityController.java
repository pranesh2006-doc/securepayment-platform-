package com.securepay.securepayment.controller;

import com.securepay.securepayment.entity.IntegrityAudit;
import com.securepay.securepayment.service.IntegrityService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/integrity")
@CrossOrigin(origins = "http://localhost:5173")
public class IntegrityController {

    private final IntegrityService integrityService;

    public IntegrityController(
            IntegrityService integrityService) {

        this.integrityService =
                integrityService;
    }

    // =====================================================
    // VERIFY TRANSACTION
    // GET /integrity/verify/{transactionId}
    // =====================================================

    @GetMapping("/verify/{transactionId}")
    public ResponseEntity<String> verifyTransaction(
            @PathVariable String transactionId) {

        String result =
                integrityService.verifyTransaction(
                        transactionId
                );

        return ResponseEntity.ok(result);
    }

    // =====================================================
    // GET AUDIT LOG
    // GET /integrity/audit/{transactionId}
    // =====================================================

    @GetMapping("/audit/{transactionId}")
    public ResponseEntity<List<IntegrityAudit>> getAuditLog(
            @PathVariable String transactionId) {

        List<IntegrityAudit> audits =
                integrityService.getAuditHistory(
                        transactionId
                );

        return ResponseEntity.ok(audits);
    }
}