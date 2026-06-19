// CancelRequestUpdateDto.java
package org.example.dto;

import org.example.domain.enums.CancelRequestStatus;

import java.util.UUID;

public record CancelRequestUpdateDto(
        CancelRequestStatus status,
        UUID processedBy
) {}