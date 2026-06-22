package org.example.service;

import lombok.AllArgsConstructor;
import org.example.domain.Booking;
import org.example.domain.enums.BookingStatus;
import org.example.domain.LoyaltyProgram;
import org.example.domain.Room;
import org.example.dto.BookingCreateDto;
import org.example.dto.BookingResponseDto;
import org.example.dto.BookingUpdateDto;
import org.example.dto.RoomResponseDto;
import org.example.mapper.BookingMapper;
import org.example.repository.BookingRepository;
import org.example.repository.LoyaltyProgramRepository;
import org.example.repository.RoomRepository;
import org.example.domain.discountStrategy.DiscountContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Validated
@AllArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final RoomRepository roomRepository;
    private final LoyaltyProgramRepository loyaltyProgramRepository;
    private final DiscountContext discountContext;
    private final RoomService roomService;

    @Transactional
    public BookingResponseDto createBooking(@Valid BookingCreateDto dto) {
        Room room = roomRepository.findByRoomId(dto.roomId())
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + dto.roomId()));

        if (room.isDeleted()) {
            throw new RuntimeException("Room is deleted");
        }

        if (dto.checkOutDate().isBefore(dto.checkInDate())) {
            throw new RuntimeException("Check-out date must be after check-in date");
        }

        List<Booking> conflicting = bookingRepository.findConflictingBookings(
                dto.roomId(), dto.checkInDate(), dto.checkOutDate());

        if (!conflicting.isEmpty()) {
            throw new RuntimeException("Room is already booked for these dates");
        }

        long nights = ChronoUnit.DAYS.between(dto.checkInDate(), dto.checkOutDate());
        BigDecimal totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));
        BigDecimal discountedPrice = applyLoyaltyDiscount(dto.userId(), totalPrice);
        Booking booking = bookingMapper.toEntity(dto);
        booking.setTotalPrice(discountedPrice);
        Booking saved = bookingRepository.save(booking);
        updateLoyaltyProgram(dto.userId(), discountedPrice);

        return bookingMapper.toDto(saved);
    }

    @Transactional
    private BigDecimal applyLoyaltyDiscount(UUID userId, BigDecimal totalPrice) {
        try {
            LoyaltyProgram loyaltyProgram = loyaltyProgramRepository.findByUserId(userId)
                    .orElse(null);

            if (loyaltyProgram == null) {
                return totalPrice;
            }

            BigDecimal discount = discountContext.calculateDiscount(
                    loyaltyProgram.getTier(),
                    totalPrice,
                    loyaltyProgram.getTotalPoints()
            );

            return totalPrice.subtract(discount);
        } catch (Exception e) {
            return totalPrice;
        }
    }

    @Transactional
    private void updateLoyaltyProgram(UUID userId, BigDecimal spentAmount) {
        try {
            LoyaltyProgram loyaltyProgram = loyaltyProgramRepository.findByUserId(userId)
                    .orElse(null);

            if (loyaltyProgram != null) {
                int pointsToAdd = spentAmount.divide(BigDecimal.valueOf(100), BigDecimal.ROUND_DOWN).intValue();
                if (pointsToAdd > 0) {
                    loyaltyProgram.setTotalPoints(loyaltyProgram.getTotalPoints() + pointsToAdd);
                }
                loyaltyProgram.setTotalSpent(loyaltyProgram.getTotalSpent().add(spentAmount));
                loyaltyProgramRepository.save(loyaltyProgram);
            }
        } catch (Exception e) {
            System.err.println("Failed to update loyalty program: " + e.getMessage());
        }
    }

    public List<BookingResponseDto> getAllBookings() {
        return bookingRepository.findByIsDeletedFalse()
                .stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    public BookingResponseDto getBooking(UUID bookingId) {
        Booking booking = bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        if (booking.isDeleted()) {
            throw new RuntimeException("Booking is deleted");
        }

        return bookingMapper.toDto(booking);
    }

    @Transactional
    public BookingResponseDto updateBooking(UUID bookingId, @Valid BookingUpdateDto dto) {
        Booking booking = bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        if (booking.isDeleted()) {
            throw new RuntimeException("Cannot update deleted booking");
        }

        if (dto.checkInDate() != null) {
            booking.setCheckInDate(dto.checkInDate());
        }

        if (dto.checkOutDate() != null) {
            booking.setCheckOutDate(dto.checkOutDate());
        }

        if (dto.status() != null) {
            booking.setStatus(dto.status());
        }

        if (dto.checkInDate() != null || dto.checkOutDate() != null) {
            Room room = roomRepository.findByRoomId(booking.getRoomId())
                    .orElseThrow(() -> new RuntimeException("Room not found"));

            LocalDate checkIn = booking.getCheckInDate();
            LocalDate checkOut = booking.getCheckOutDate();

            long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
            if (nights <= 0) {
                throw new RuntimeException("Check-out date must be after check-in date");
            }

            BigDecimal totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));
            BigDecimal discountedPrice = applyLoyaltyDiscount(booking.getUserId(), totalPrice);
            booking.setTotalPrice(discountedPrice);
        }

        Booking updated = bookingRepository.save(booking);
        return bookingMapper.toDto(updated);
    }

    @Transactional
    public void cancelBooking(UUID bookingId) {
        Booking booking = bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        if (booking.isDeleted()) {
            throw new RuntimeException("Booking is deleted");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    @Transactional
    public void confirmBooking(UUID bookingId) {
        Booking booking = bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        if (booking.isDeleted()) {
            throw new RuntimeException("Booking is deleted");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
    }

    @Transactional
    public void softDeleteBooking(UUID bookingId) {
        Booking booking = bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        booking.setDeleted(true);
        bookingRepository.save(booking);
    }

    public List<BookingResponseDto> getBookingsByUser(UUID userId) {
        return bookingRepository.findByUserIdAndIsDeletedFalse(userId)
                .stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }
    public Page<BookingResponseDto> getBookingsByUser(UUID userId, Pageable pageable) {
        return bookingRepository.findByUserIdAndIsDeletedFalse(userId, pageable)
                .map(bookingMapper::toDto);
    }

    public Page<BookingResponseDto> getBookingsByHotel(UUID hotelId, Pageable pageable) {
        List<UUID> roomIds = roomService.getRoomsByHotel(hotelId).stream()
                .map(RoomResponseDto::roomId)
                .collect(Collectors.toList());

        return bookingRepository.findByRoomIdInAndIsDeletedFalse(roomIds, pageable)
                .map(bookingMapper::toDto);
    }

    public List<BookingResponseDto> getBookingsByStatus(BookingStatus status) {
        return bookingRepository.findByStatusAndIsDeletedFalse(status)
                .stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }
}