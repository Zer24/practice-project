package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.domain.RoomType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponseDto {
    private UUID roomId;
    private UUID hotelId;
    private RoomType roomType;
    private BigDecimal pricePerNight;
    private Integer capacity;
    private String description;
    private List<String> amenities;
    private Double area;
}