package org.example.domain;

import java.math.BigDecimal;

public class SilverDiscountStrategy implements LoyaltyDiscountStrategy {
    private static final double DISCOUNT_RATE = 0.10; // 10%
    private static final int MIN_POINTS = 500;

    @Override
    public BigDecimal calculateDiscount(BigDecimal amount, int loyaltyPoints) {
        if (loyaltyPoints < MIN_POINTS) {
            return BigDecimal.ZERO;
        }
        double bonusRate = Math.min(loyaltyPoints / 10000.0, 0.05);
        double totalRate = DISCOUNT_RATE + bonusRate;
        return amount.multiply(BigDecimal.valueOf(Math.min(totalRate, 0.20)));
    }

    @Override
    public String getStrategyName() {
        return "SILVER";
    }
}