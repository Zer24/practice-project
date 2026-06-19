// RoomUpdateDto.java
package org.example.dto;

import jakarta.validation.constraints.Min;
import org.example.domain.enums.RoomType;

import java.math.BigDecimal;
import java.util.List;

public record RoomUpdateDto(
        RoomType roomType,
        @Min(value = 0, message = "Price must be positive")
        BigDecimal pricePerNight,
        @Min(value = 1, message = "Capacity must be at least 1")
        Integer capacity,
        String description,
        List<String> amenities,
        @Min(value = 0, message = "Area must be positive")
        Double area
) {}