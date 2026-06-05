package com.zbank.fraud.controller;

import com.zbank.fraud.event.FraudAlertEvent;
import com.zbank.fraud.model.TransactionRequest;
import com.zbank.fraud.repository.BlacklistedMerchantRepository;
import com.zbank.fraud.service.VelocityFraudDetector;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/fraud")
public class FraudCheckController {

    private final VelocityFraudDetector velocityFraudDetector;
    private final BlacklistedMerchantRepository merchantRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Inject KafkaTemplate into the constructor
    public FraudCheckController(VelocityFraudDetector velocityFraudDetector,
                                BlacklistedMerchantRepository merchantRepository,
                                KafkaTemplate<String, Object> kafkaTemplate) {
        this.velocityFraudDetector = velocityFraudDetector;
        this.merchantRepository = merchantRepository;
        this.kafkaTemplate = kafkaTemplate;
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
                        request.merchantCategoryCode(),
                        Instant.now()
                );

        // 1. PostgreSQL Check
        if (merchantRepository.existsByMerchantCategoryCode(processedRequest.merchantCategoryCode())) {
            publishFraudAlert(processedRequest, "DECLINED_BLACKLISTED_MERCHANT");
            return ResponseEntity.ok(new FraudCheckResponse(processedRequest.transactionId(), true, "DECLINED_BLACKLISTED_MERCHANT"));
        }

        // 2. Redis Check
        if (velocityFraudDetector.isFraudulentVelocity(processedRequest)) {
            publishFraudAlert(processedRequest, "DECLINED_VELOCITY_LIMIT_EXCEEDED");
            return ResponseEntity.ok(new FraudCheckResponse(processedRequest.transactionId(), true, "DECLINED_VELOCITY_LIMIT_EXCEEDED"));
        }

        return ResponseEntity.ok(new FraudCheckResponse(processedRequest.transactionId(), false, "APPROVED"));
    }

    // Helper method to publish the event asynchronously 
    private void publishFraudAlert(TransactionRequest request, String reason) {
        FraudAlertEvent event = new FraudAlertEvent(
                request.transactionId(),
                request.cardId(),
                request.amount(),
                reason,
                Instant.now()
        );
        // Fire and forget: send to the "fraud-alerts" topic
        kafkaTemplate.send("fraud-alerts", event);
    }

    public record FraudCheckResponse(String transactionId, boolean isFraudulent, String statusReason) {}
}