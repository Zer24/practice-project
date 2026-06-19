package org.example.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record BookingCreateDto(
        @NotNull(message = "User ID is required")
        UUID userId,

        @NotNull(message = "Room ID is required")
        UUID roomId,

        @NotNull(message = "Check-in date is required")
        @Future(message = "Check-in date must be in the future")
        LocalDate checkInDate,

        @NotNull(message = "Check-out date is required")
        @Future(message = "Check-out date must be in the future")
        LocalDate checkOutDate
) {}