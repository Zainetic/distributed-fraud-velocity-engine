package com.zbank.fraud.service;

import com.zbank.fraud.model.TransactionRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class VelocityFraudDetectorTest {

    @Autowired
    private VelocityFraudDetector velocityFraudDetector;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final String TEST_CARD_ID = "card_987654321";

    @AfterEach
    void cleanUp() {
        // Clear Redis after each test so we start fresh
        redisTemplate.delete("velocity:card:" + TEST_CARD_ID);
    }

    @Test
    void shouldFlagFraudWhenVelocityExceedsThreeTransactionsInSixtySeconds() {
        // Arrange: Create a base transaction timestamp
        Instant now = Instant.now();

        // Act & Assert: Simulate 3 rapid, valid transactions
        for (int i = 0; i < 3; i++) {
            TransactionRequest validReq = new TransactionRequest(
                    UUID.randomUUID().toString(),
                    TEST_CARD_ID,
                    "acc_123",
                    new BigDecimal("25.00"),
                    "EUR",
                    now.plusSeconds(i) // Space them out by 1 second
            );
            
            boolean isFraud = velocityFraudDetector.isFraudulentVelocity(validReq);
            assertFalse(isFraud, "Transaction " + (i + 1) + " should NOT be flagged as fraud");
        }

        // Act & Assert: The 4th transaction within the window MUST be flagged
        TransactionRequest fraudulentReq = new TransactionRequest(
                UUID.randomUUID().toString(),
                TEST_CARD_ID,
                "acc_123",
                new BigDecimal("500.00"),
                "EUR",
                now.plusSeconds(4)
        );

        boolean isFraudulent = velocityFraudDetector.isFraudulentVelocity(fraudulentReq);
        assertTrue(isFraudulent, "The 4th transaction MUST be flagged as velocity fraud");
    }
}