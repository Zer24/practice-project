package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelRequestDto {
    private UUID requestId;
    private UUID bookingId;
    private UUID userId;
    private String reason;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private UUID processedBy;
}