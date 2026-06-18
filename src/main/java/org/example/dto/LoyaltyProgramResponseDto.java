package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.domain.LoyaltyTier;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyProgramResponseDto {
    private UUID loyaltyId;
    private UUID userId;
    private Integer totalPoints;
    private LoyaltyTier tier;
    private BigDecimal totalSpent;
}