// CancelRequestDto.java
package org.example.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CancelRequestDto(
        UUID requestId,
        UUID bookingId,
        UUID userId,
        String reason,
        String status,
        LocalDateTime createdAt,
        LocalDateTime processedAt,
        UUID processedBy
) {}