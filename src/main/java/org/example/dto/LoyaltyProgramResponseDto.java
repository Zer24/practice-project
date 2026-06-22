package org.example.dto;

import org.example.domain.enums.LoyaltyTier;

import java.math.BigDecimal;
import java.util.UUID;

public record LoyaltyProgramResponseDto(
        UUID loyaltyId,
        UUID userId,
        Integer totalPoints,
        LoyaltyTier tier,
        BigDecimal totalSpent
) {}