package com.zbank.fraud.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "blacklisted_merchants")
public class BlacklistedMerchant {

    @Id
    private String merchantCategoryCode;
    
    private String riskReason;
    
    private Instant addedAt;

    // JPA Requires a no-args constructor
    protected BlacklistedMerchant() {}

    public BlacklistedMerchant(String merchantCategoryCode, String riskReason) {
        this.merchantCategoryCode = merchantCategoryCode;
        this.riskReason = riskReason;
        this.addedAt = Instant.now();
    }

    public String getMerchantCategoryCode() {
        return merchantCategoryCode;
    }

    public String getRiskReason() {
        return riskReason;
    }
}