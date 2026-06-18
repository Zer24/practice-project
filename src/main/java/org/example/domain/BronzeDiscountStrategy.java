package org.example.domain;

import java.math.BigDecimal;

public class BronzeDiscountStrategy implements LoyaltyDiscountStrategy {
    private static final double DISCOUNT_RATE = 0.05; // 5%

    @Override
    public BigDecimal calculateDiscount(BigDecimal amount, int loyaltyPoints) {
        if (loyaltyPoints < 100) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(BigDecimal.valueOf(DISCOUNT_RATE));
    }

    @Override
    public String getStrategyName() {
        return "BRONZE";
    }
}