package org.example.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogDto(
        UUID auditId,
        String action,
        String entityType,
        String entityId,
        UUID performedBy,
        LocalDateTime performedAt,
        String oldValue,
        String newValue,
        String details
) {}