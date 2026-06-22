package org.example.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record HotelCreateDto(
        @NotBlank(message = "Hotel name is required")
        String name,

        @NotBlank(message = "City is required")
        String city,

        @NotBlank(message = "Country is required")
        String country,

        @NotBlank(message = "Address is required")
        String address,

        UUID managerId
) {}