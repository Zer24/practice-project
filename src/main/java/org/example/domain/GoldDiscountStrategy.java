package org.example.domain;

import java.math.BigDecimal;

public class GoldDiscountStrategy implements LoyaltyDiscountStrategy {
    private static final double DISCOUNT_RATE = 0.15; // 15%
    private static final int MIN_POINTS = 1000;

    @Override
    public BigDecimal calculateDiscount(BigDecimal amount, int loyaltyPoints) {
        if (loyaltyPoints < MIN_POINTS) {
            return BigDecimal.ZERO;
        }
        double bonusRate = Math.min(loyaltyPoints / 5000.0, 0.10);
        double totalRate = DISCOUNT_RATE + bonusRate;
        return amount.multiply(BigDecimal.valueOf(Math.min(totalRate, 0.30)));
    }

    @Override
    public String getStrategyName() {
        return "GOLD";
    }
}