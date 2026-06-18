package org.example.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.domain.LoyaltyTier;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyProgramUpdateDto {

    @Min(value = 0, message = "Points cannot be negative")
    private Integer totalPoints;

    private LoyaltyTier tier;

    @Min(value = 0, message = "Total spent cannot be negative")
    private BigDecimal totalSpent;
}