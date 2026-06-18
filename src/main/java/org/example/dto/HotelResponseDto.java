package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.domain.Hotel;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelResponseDto {
    private UUID hotelId;
    private String name;
    private String city;
    private String country;
    private String address;
    private Double rating;
    private UUID managerId;

    public HotelResponseDto(Hotel hotel) {
        this.hotelId = hotel.getHotelId();
        this.name = hotel.getName();
        this.city = hotel.getCity();
        this.country = hotel.getCountry();
        this.address = hotel.getAddress();
        this.rating = hotel.getRating();
        this.managerId = hotel.getManagerId();
    }
}