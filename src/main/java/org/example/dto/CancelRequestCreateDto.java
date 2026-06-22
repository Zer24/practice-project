package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CancelRequestCreateDto(
        @NotNull(message = "Booking ID is required")
        UUID bookingId,

        @NotBlank(message = "Reason is required")
        String reason
) {}