package org.example.domain;

import org.example.domain.LoyaltyTier;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

public class DiscountContext {
    private final Map<LoyaltyTier, LoyaltyDiscountStrategy> strategies = new EnumMap<>(LoyaltyTier.class);

    public DiscountContext() {
        // Инициализация стратегий
        strategies.put(LoyaltyTier.BRONZE, new BronzeDiscountStrategy());
        strategies.put(LoyaltyTier.SILVER, new SilverDiscountStrategy());
        strategies.put(LoyaltyTier.GOLD, new GoldDiscountStrategy());
        strategies.put(LoyaltyTier.PLATINUM, new PlatinumDiscountStrategy());
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