package org.example.domain.discountStrategy;

import org.example.config.BusinessProperties;

import java.math.BigDecimal;

public class GoldDiscountStrategy implements LoyaltyDiscountStrategy {
    private final BusinessProperties properties;
    public GoldDiscountStrategy(BusinessProperties properties) {
        this.properties = properties;
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal amount, int loyaltyPoints) {
        if (loyaltyPoints < properties.getLoyalty().getMinPointsForDiscount().get("gold")) {
            return BigDecimal.ZERO;
        }
        double discountRate = properties.getLoyalty().getBaseDiscountRates().get("gold");
        return amount.multiply(BigDecimal.valueOf(discountRate));
    }

    @Override
    public String getStrategyName() {
        return "GOLD";
    }
}