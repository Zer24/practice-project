package org.example.domain;

import java.math.BigDecimal;

public class PlatinumDiscountStrategy implements LoyaltyDiscountStrategy {
    private static final double DISCOUNT_RATE = 0.20; // 20%
    private static final int MIN_POINTS = 5000;

    @Override
    public BigDecimal calculateDiscount(BigDecimal amount, int loyaltyPoints) {
        if (loyaltyPoints < MIN_POINTS) {
            return BigDecimal.ZERO;
        }
        double bonusRate = Math.min(loyaltyPoints / 2000.0, 0.15);
        double totalRate = DISCOUNT_RATE + bonusRate;
        return amount.multiply(BigDecimal.valueOf(Math.min(totalRate, 0.40)));
    }

    @Override
    public String getStrategyName() {
        return "PLATINUM";
    }
}