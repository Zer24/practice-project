package org.example.domain;

import java.math.BigDecimal;

public interface LoyaltyDiscountStrategy {
    BigDecimal calculateDiscount(BigDecimal amount, int loyaltyPoints);
    String getStrategyName();
}