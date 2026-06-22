package org.example.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.domain.enums.RoomType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record RoomCreateDto(
        @NotNull(message = "Hotel ID is required")
        UUID hotelId,

        @NotNull(message = "Room type is required")
        RoomType roomType,

        @NotNull(message = "Price per night is required")
        @Min(value = 0, message = "Price must be positive")
        BigDecimal pricePerNight,

        @NotNull(message = "Capacity is required")
        @Min(value = 1, message = "Capacity must be at least 1")
        Integer capacity,

        @NotBlank(message = "Description is required")
        String description,

        List<String> amenities,

        @Min(value = 0, message = "Area must be positive")
        Double area
) {}