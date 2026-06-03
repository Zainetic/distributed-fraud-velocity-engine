package com.zbank.fraud.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Immutable representation of an incoming transaction payload.
 * Used across the Velocity Engine for fraud evaluation.
 */
public record TransactionRequest(
    String transactionId,
    String cardId,
    String accountId,
    BigDecimal amount,
    String currency,
    Instant timestamp
) {}