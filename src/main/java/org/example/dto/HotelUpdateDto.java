package org.example.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelUpdateDto {

    private String name;

    private String city;

    private String country;

    private String address;

    @Min(0)
    @Max(5)
    private Double rating;

    private UUID managerId;
}