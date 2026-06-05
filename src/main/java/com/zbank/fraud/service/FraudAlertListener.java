package com.zbank.fraud.service;

import com.zbank.fraud.event.FraudAlertEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class FraudAlertListener {

    private static final Logger log = LoggerFactory.getLogger(FraudAlertListener.class);

    // This method listens to the topic. The moment an event drops in, this fires automatically.
    @KafkaListener(topics = "fraud-alerts", groupId = "zbank-fraud-group")
    public void consumeFraudAlert(FraudAlertEvent event) {
        log.warn("🚨 ASYNC KAFKA ALERT RECEIVED: Account freeze initiated for Card {} due to: {}", 
                 event.cardId(), event.declineReason());
    }
}