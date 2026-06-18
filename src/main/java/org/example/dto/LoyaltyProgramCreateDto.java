package org.example.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.domain.LoyaltyTier;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyProgramCreateDto {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Total points is required")
    @Min(value = 0, message = "Points cannot be negative")
    private Integer totalPoints;

    @NotNull(message = "Tier is required")
    private LoyaltyTier tier;
}