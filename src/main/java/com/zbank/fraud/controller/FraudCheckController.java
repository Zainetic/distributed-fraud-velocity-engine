package com.zbank.fraud.controller;

import com.zbank.fraud.model.TransactionRequest;
import com.zbank.fraud.service.VelocityFraudDetector;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/fraud")
public class FraudCheckController {

    private final VelocityFraudDetector velocityFraudDetector;

    public FraudCheckController(VelocityFraudDetector velocityFraudDetector) {
        this.velocityFraudDetector = velocityFraudDetector;
    }

    @PostMapping("/velocity-check")
    public ResponseEntity<FraudCheckResponse> checkVelocity(@RequestBody TransactionRequest request) {
        
        // Safety net: If the upstream payment gateway doesn't provide a timestamp, generate one.
        TransactionRequest processedRequest = request.timestamp() != null ? request :
                new TransactionRequest(
                        request.transactionId(),
                        request.cardId(),
                        request.accountId(),
                        request.amount(),
                        request.currency(),
                        Instant.now()
                );

        // Execute the Redis sliding-window algorithm
        boolean isFraudulent = velocityFraudDetector.isFraudulentVelocity(processedRequest);

        // Construct the immutable response payload
        FraudCheckResponse response = new FraudCheckResponse(
                processedRequest.transactionId(),
                isFraudulent,
                isFraudulent ? "DECLINED_VELOCITY_LIMIT_EXCEEDED" : "APPROVED"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Immutable response record representing the outcome of the fraud check.
     */
    public record FraudCheckResponse(
            String transactionId, 
            boolean isFraudulent, 
            String statusReason
    ) {}
}