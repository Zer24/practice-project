package org.example.dto;

import org.example.domain.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BookingResponseDto(
        UUID bookingId,
        UUID userId,
        UUID roomId,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        BigDecimal totalPrice,
        BookingStatus status
) {}