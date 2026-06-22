package org.example.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.UUID;

public record HotelUpdateDto(
        String name,
        String city,
        String country,
        String address,
        @Min(0) @Max(5) Double rating,
        UUID managerId
) {}