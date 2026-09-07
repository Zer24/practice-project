package org.example.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record LoyaltyTransactionDto(
        UUID transactionId,
        String type,           // "EARNED", "REDEEMED", "SPENT"
        Integer points,
        BigDecimal amount,
        String description,
        LocalDateTime createdAt
) {}