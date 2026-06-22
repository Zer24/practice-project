package org.example.dto;

import org.example.domain.Hotel;

import java.util.UUID;

public record HotelResponseDto(
        UUID hotelId,
        String name,
        String city,
        String country,
        String address,
        Double rating,
        UUID managerId
) {
    public HotelResponseDto(Hotel hotel) {
        this(
                hotel.getHotelId(),
                hotel.getName(),
                hotel.getCity(),
                hotel.getCountry(),
                hotel.getAddress(),
                hotel.getRating(),
                hotel.getManagerId()
        );
    }
}