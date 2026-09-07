package org.example.dto;

import org.example.domain.enums.BookingStatus;

public record BookingStatusUpdateDto(
        BookingStatus status
) {}