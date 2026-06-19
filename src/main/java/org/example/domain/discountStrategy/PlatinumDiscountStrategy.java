package org.example.domain.discountStrategy;

import org.example.config.BusinessProperties;

import java.math.BigDecimal;

public class PlatinumDiscountStrategy implements LoyaltyDiscountStrategy {
    private final BusinessProperties properties;
    public PlatinumDiscountStrategy(BusinessProperties properties) {
        this.properties = properties;
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal amount, int loyaltyPoints) {
        if (loyaltyPoints < properties.getLoyalty().getMinPointsForDiscount().get("platinum")) {
            return BigDecimal.ZERO;
        }
        double discountRate = properties.getLoyalty().getBaseDiscountRates().get("platinum");
        return amount.multiply(BigDecimal.valueOf(discountRate));
    }

    @Override
    public String getStrategyName() {
        return "PLATINUM";
    }
}