package org.example.domain.discountStrategy;

import org.example.domain.enums.LoyaltyTier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class DiscountContext {
    private final Map<LoyaltyTier, LoyaltyDiscountStrategy> strategies = new EnumMap<>(LoyaltyTier.class);

    public DiscountContext(List<LoyaltyDiscountStrategy> strategyList) {
        for (LoyaltyDiscountStrategy strategy : strategyList) {
            LoyaltyTier tier = LoyaltyTier.valueOf(strategy.getStrategyName());
            strategies.put(tier, strategy);
        }
    }

    public BigDecimal calculateDiscount(LoyaltyTier tier, BigDecimal amount, int loyaltyPoints) {
        LoyaltyDiscountStrategy strategy = strategies.get(tier);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for tier: " + tier);
        }
        return strategy.calculateDiscount(amount, loyaltyPoints);
    }

    public void setStrategy(LoyaltyTier tier, LoyaltyDiscountStrategy strategy) {
        strategies.put(tier, strategy);
    }

    public LoyaltyDiscountStrategy getStrategy(LoyaltyTier tier) {
        return strategies.get(tier);
    }
}