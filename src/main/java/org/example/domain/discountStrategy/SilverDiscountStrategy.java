package org.example.domain.discountStrategy;

import org.example.config.BusinessProperties;

import java.math.BigDecimal;

public class SilverDiscountStrategy implements LoyaltyDiscountStrategy {
    private final BusinessProperties properties;
    public SilverDiscountStrategy(BusinessProperties properties) {
        this.properties = properties;
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal amount, int loyaltyPoints) {
        if (loyaltyPoints < properties.getLoyalty().getMinPointsForDiscount().get("silver")) {
            return BigDecimal.ZERO;
        }
        double discountRate = properties.getLoyalty().getBaseDiscountRates().get("silver");
        return amount.multiply(BigDecimal.valueOf(discountRate));
    }

    @Override
    public String getStrategyName() {
        return "SILVER";
    }
}