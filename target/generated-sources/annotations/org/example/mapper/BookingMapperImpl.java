package org.example.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.example.domain.Booking;
import org.example.domain.enums.BookingStatus;
import org.example.dto.BookingCreateDto;
import org.example.dto.BookingResponseDto;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-19T23:11:00+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class BookingMapperImpl implements BookingMapper {

    @Override
    public BookingResponseDto toDto(Booking booking) {
        if ( booking == null ) {
            return null;
        }

        UUID bookingId = null;
        UUID userId = null;
        UUID roomId = null;
        LocalDate checkInDate = null;
        LocalDate checkOutDate = null;
        BigDecimal totalPrice = null;
        BookingStatus status = null;

        bookingId = booking.getBookingId();
        userId = booking.getUserId();
        roomId = booking.getRoomId();
        checkInDate = booking.getCheckInDate();
        checkOutDate = booking.getCheckOutDate();
        totalPrice = booking.getTotalPrice();
        status = booking.getStatus();

        BookingResponseDto bookingResponseDto = new BookingResponseDto( bookingId, userId, roomId, checkInDate, checkOutDate, totalPrice, status );

        return bookingResponseDto;
    }

    @Override
    public Booking toEntity(BookingCreateDto dto) {
        if ( dto == null ) {
            return null;
        }

        Booking booking = new Booking();

        booking.setUserId( dto.userId() );
        booking.setRoomId( dto.roomId() );
        booking.setCheckInDate( dto.checkInDate() );
        booking.setCheckOutDate( dto.checkOutDate() );

        booking.setStatus( BookingStatus.CREATED );

        generateBookingId( booking );

        return booking;
    }
}
