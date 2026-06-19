package org.example.domain.discountStrategy;

import java.math.BigDecimal;

public interface LoyaltyDiscountStrategy {
    BigDecimal calculateDiscount(BigDecimal amount, int loyaltyPoints);
    String getStrategyName();
}