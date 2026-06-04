package com.zbank.fraud.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Immutable representation of an incoming transaction payload.
 * Includes strict validation constraints for API boundary safety.
 */
public record TransactionRequest(
    @NotBlank(message = "Transaction ID is required")
    String transactionId,
    
    @NotBlank(message = "Card ID is required")
    String cardId,
    
    @NotBlank(message = "Account ID is required")
    String accountId,
    
    @NotNull(message = "Transaction amount cannot be null")
    @Positive(message = "Transaction amount must be strictly positive")
    BigDecimal amount,
    
    @NotBlank(message = "Currency code is required")
    String currency,
    
    Instant timestamp
) {}