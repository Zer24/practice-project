package org.example.mapper;

import javax.annotation.processing.Generated;
import org.example.domain.Booking;
import org.example.domain.BookingStatus;
import org.example.dto.BookingCreateDto;
import org.example.dto.BookingResponseDto;
import org.example.dto.BookingUpdateDto;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-18T13:51:36+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class BookingMapperImpl implements BookingMapper {

    @Override
    public BookingResponseDto toDto(Booking booking) {
        if ( booking == null ) {
            return null;
        }

        BookingResponseDto bookingResponseDto = new BookingResponseDto();

        bookingResponseDto.setBookingId( booking.getBookingId() );
        bookingResponseDto.setUserId( booking.getUserId() );
        bookingResponseDto.setRoomId( booking.getRoomId() );
        bookingResponseDto.setCheckInDate( booking.getCheckInDate() );
        bookingResponseDto.setCheckOutDate( booking.getCheckOutDate() );
        bookingResponseDto.setTotalPrice( booking.getTotalPrice() );
        bookingResponseDto.setStatus( booking.getStatus() );

        return bookingResponseDto;
    }

    @Override
    public Booking toEntity(BookingCreateDto dto) {
        if ( dto == null ) {
            return null;
        }

        Booking booking = new Booking();

        booking.setUserId( dto.getUserId() );
        booking.setRoomId( dto.getRoomId() );
        booking.setCheckInDate( dto.getCheckInDate() );
        booking.setCheckOutDate( dto.getCheckOutDate() );

        booking.setStatus( BookingStatus.CREATED );

        generateBookingId( booking );

        return booking;
    }

    @Override
    public void updateEntity(Booking booking, BookingUpdateDto dto) {
        if ( dto == null ) {
            return;
        }

        booking.setCheckInDate( dto.getCheckInDate() );
        booking.setCheckOutDate( dto.getCheckOutDate() );
        booking.setStatus( dto.getStatus() );

        generateBookingId( booking );
    }
}
