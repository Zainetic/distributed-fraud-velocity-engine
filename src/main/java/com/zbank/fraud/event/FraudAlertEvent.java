package com.zbank.fraud.event;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Immutable event published to Kafka whenever a transaction is declined.
 */
public record FraudAlertEvent(
        String transactionId,
        String cardId,
        BigDecimal attemptedAmount,
        String declineReason,
        Instant timestamp
) {}