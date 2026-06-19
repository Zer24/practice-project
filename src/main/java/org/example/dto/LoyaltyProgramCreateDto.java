// LoyaltyProgramCreateDto.java
package org.example.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.example.domain.enums.LoyaltyTier;

import java.util.UUID;

public record LoyaltyProgramCreateDto(
        @NotNull(message = "User ID is required")
        UUID userId,

        @NotNull(message = "Total points is required")
        @Min(value = 0, message = "Points cannot be negative")
        Integer totalPoints,

        @NotNull(message = "Tier is required")
        LoyaltyTier tier
) {}