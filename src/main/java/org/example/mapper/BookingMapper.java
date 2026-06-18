package org.example.mapper;

import org.example.domain.Booking;
import org.example.domain.Hotel;
import org.example.dto.BookingCreateDto;
import org.example.dto.BookingResponseDto;
import org.example.dto.BookingUpdateDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    BookingResponseDto toDto(Booking booking);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bookingId", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "status", constant = "CREATED")
    @Mapping(target = "deleted", ignore = true)
    Booking toEntity(BookingCreateDto dto);

    @AfterMapping
    default void generateBookingId(@MappingTarget Booking booking) {
        booking.setBookingId(UUID.randomUUID());
    }
}