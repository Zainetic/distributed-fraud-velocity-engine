package com.zbank.fraud.service;

import com.zbank.fraud.model.TransactionRequest;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VelocityFraudDetector {

    private final StringRedisTemplate redisTemplate;
    
    // Core financial rule constraints
    private static final int MAX_TRANSACTIONS_PER_WINDOW = 3;
    private static final long TIME_WINDOW_SECONDS = 60;

    public VelocityFraudDetector(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Evaluates a transaction request against historical velocity rules.
     * @return true if the transaction exceeds thresholds (Fraud Flagged), false if allowed.
     */
    public boolean isFraudulentVelocity(TransactionRequest request) {
        String redisKey = "velocity:card:" + request.cardId();
        long currentTimeMillis = request.timestamp().toEpochMilli();
        long windowStartMillis = currentTimeMillis - (TIME_WINDOW_SECONDS * 1000);
        String transactionId = request.transactionId();

        // Execute batch operations via a pipeline to optimize network round-trips
        List<Object> pipelineResults = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            byte[] rawKey = redisTemplate.getStringSerializer().serialize(redisKey);
            byte[] rawMember = redisTemplate.getStringSerializer().serialize(transactionId);

            // 1. Add current transaction to the Sorted Set (Score = Timestamp)
            connection.zSetCommands().zAdd(rawKey, currentTimeMillis, rawMember);

            // 2. Evict expired entries older than the rolling window
            connection.zSetCommands().zRemRangeByScore(rawKey, 0, windowStartMillis);

            // 3. Count the remaining active transactions inside the window
            connection.zSetCommands().zCard(rawKey);

            // 4. Set a Time-To-Live expiration to prevent data leaks in RAM
            connection.keyCommands().expire(rawKey, TIME_WINDOW_SECONDS);

            return null; // Pipeline requires returning null here
        });

        // The result of zCard (the 3rd command executed) is located at index 2
        Long transactionCount = (Long) pipelineResults.get(2);

        // If the card has processed more than our allowable limit, flag it as fraudulent
        return transactionCount != null && transactionCount > MAX_TRANSACTIONS_PER_WINDOW;
    }
}