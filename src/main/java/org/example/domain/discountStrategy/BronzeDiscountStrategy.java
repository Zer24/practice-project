package org.example.domain.discountStrategy;

import org.example.config.BusinessProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BronzeDiscountStrategy implements LoyaltyDiscountStrategy {

    private final BusinessProperties properties;
    public BronzeDiscountStrategy(BusinessProperties properties) {
        this.properties = properties;
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal amount, int loyaltyPoints) {
        if (loyaltyPoints < properties.getLoyalty().getMinPointsForDiscount().get("bronze")) {
            return BigDecimal.ZERO;
        }
        double discountRate = properties.getLoyalty().getBaseDiscountRates().get("bronze");
        return amount.multiply(BigDecimal.valueOf(discountRate));
    }

    @Override
    public String getStrategyName() {
        return "BRONZE";
    }
}