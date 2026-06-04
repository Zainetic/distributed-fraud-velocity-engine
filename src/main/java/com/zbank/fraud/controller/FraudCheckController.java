package com.zbank.fraud.controller;

import com.zbank.fraud.model.TransactionRequest;
import com.zbank.fraud.repository.BlacklistedMerchantRepository;
import com.zbank.fraud.service.VelocityFraudDetector;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/fraud")
public class FraudCheckController {

    private final VelocityFraudDetector velocityFraudDetector;
    private final BlacklistedMerchantRepository merchantRepository;

    public FraudCheckController(VelocityFraudDetector velocityFraudDetector, 
                                BlacklistedMerchantRepository merchantRepository) {
        this.velocityFraudDetector = velocityFraudDetector;
        this.merchantRepository = merchantRepository;
    }

    @PostMapping("/velocity-check")
    public ResponseEntity<FraudCheckResponse> checkVelocity(@Valid @RequestBody TransactionRequest request) {
        
        TransactionRequest processedRequest = request.timestamp() != null ? request :
                new TransactionRequest(
                        request.transactionId(),
                        request.cardId(),
                        request.accountId(),
                        request.amount(),
                        request.currency(),
                        request.merchantCategoryCode(), // New field
                        Instant.now()
                );

        // 1. PostgreSQL Check: Is this a blacklisted merchant?
        if (merchantRepository.existsByMerchantCategoryCode(processedRequest.merchantCategoryCode())) {
            return ResponseEntity.ok(new FraudCheckResponse(
                    processedRequest.transactionId(),
                    true,
                    "DECLINED_BLACKLISTED_MERCHANT"
            ));
        }

        // 2. Redis Check: Is the card swiping too fast?
        boolean isFraudulentVelocity = velocityFraudDetector.isFraudulentVelocity(processedRequest);

        if (isFraudulentVelocity) {
            return ResponseEntity.ok(new FraudCheckResponse(
                    processedRequest.transactionId(),
                    true,
                    "DECLINED_VELOCITY_LIMIT_EXCEEDED"
            ));
        }

        return ResponseEntity.ok(new FraudCheckResponse(
                processedRequest.transactionId(),
                false,
                "APPROVED"
        ));
    }

    public record FraudCheckResponse(String transactionId, boolean isFraudulent, String statusReason) {}
}