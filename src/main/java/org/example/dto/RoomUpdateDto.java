package org.example.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.domain.RoomType;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomUpdateDto {

    private RoomType roomType;

    @Min(value = 0, message = "Price must be positive")
    private BigDecimal pricePerNight;

    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    private String description;

    private List<String> amenities;

    @Min(value = 0, message = "Area must be positive")
    private Double area;
}