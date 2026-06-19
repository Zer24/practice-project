// RoomResponseDto.java
package org.example.dto;

import org.example.domain.enums.RoomType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record RoomResponseDto(
        UUID roomId,
        UUID hotelId,
        RoomType roomType,
        BigDecimal pricePerNight,
        Integer capacity,
        String description,
        List<String> amenities,
        Double area
) {}