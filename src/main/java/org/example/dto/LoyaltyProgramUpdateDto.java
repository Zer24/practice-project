// LoyaltyProgramUpdateDto.java
package org.example.dto;

import jakarta.validation.constraints.Min;
import org.example.domain.enums.LoyaltyTier;

import java.math.BigDecimal;

public record LoyaltyProgramUpdateDto(
        @Min(value = 0, message = "Points cannot be negative")
        Integer totalPoints,

        LoyaltyTier tier,

        @Min(value = 0, message = "Total spent cannot be negative")
        BigDecimal totalSpent
) {}