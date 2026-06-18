package org.example.mapper;

import org.example.domain.Hotel;
import org.example.domain.User;
import org.example.dto.HotelCreateDto;
import org.example.dto.HotelResponseDto;
import org.example.dto.HotelUpdateDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface HotelMapper {

    HotelResponseDto toDto(Hotel hotel);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hotelId", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "rating", ignore = true)
    Hotel toEntity(HotelCreateDto dto);

    @AfterMapping
    default void generateHotelId(@MappingTarget Hotel hotel) {
        if (hotel.getHotelId() == null) {
            hotel.setHotelId(UUID.randomUUID());
        }
    }
}