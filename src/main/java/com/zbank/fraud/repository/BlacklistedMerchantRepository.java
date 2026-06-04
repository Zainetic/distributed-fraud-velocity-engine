package com.zbank.fraud.repository;

import com.zbank.fraud.entity.BlacklistedMerchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlacklistedMerchantRepository extends JpaRepository<BlacklistedMerchant, String> {
    
    // Spring Data automatically implements this SQL: 
    // SELECT count(*) > 0 FROM blacklisted_merchants WHERE merchant_category_code = ?
    boolean existsByMerchantCategoryCode(String merchantCategoryCode);
}