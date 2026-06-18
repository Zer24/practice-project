package org.example.service;

import org.example.domain.Booking;
import org.example.domain.BookingStatus;
import org.example.domain.LoyaltyProgram;
import org.example.domain.Room;
import org.example.dto.BookingCreateDto;
import org.example.dto.BookingResponseDto;
import org.example.dto.BookingUpdateDto;
import org.example.mapper.BookingMapper;
import org.example.repository.BookingRepository;
import org.example.repository.LoyaltyProgramRepository;
import org.example.repository.RoomRepository;
import org.example.domain.DiscountContext;
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
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final RoomRepository roomRepository;
    private final LoyaltyProgramRepository loyaltyProgramRepository;
    private final DiscountContext discountContext;

    public BookingService(BookingRepository bookingRepository,
                          BookingMapper bookingMapper,
                          RoomRepository roomRepository,
                          LoyaltyProgramRepository loyaltyProgramRepository,
                          DiscountContext discountContext) {
        this.bookingRepository = bookingRepository;
        this.bookingMapper = bookingMapper;
        this.roomRepository = roomRepository;
        this.loyaltyProgramRepository = loyaltyProgramRepository;
        this.discountContext = discountContext;
    }

    public BookingResponseDto createBooking(@Valid BookingCreateDto dto) {
        Room room = roomRepository.findByRoomId(dto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + dto.getRoomId()));

        if (room.isDeleted()) {
            throw new RuntimeException("Room is deleted");
        }

        if (dto.getCheckOutDate().isBefore(dto.getCheckInDate())) {
            throw new RuntimeException("Check-out date must be after check-in date");
        }

        List<Booking> conflicting = bookingRepository.findConflictingBookings(
                dto.getRoomId(), dto.getCheckInDate(), dto.getCheckOutDate());

        if (!conflicting.isEmpty()) {
            throw new RuntimeException("Room is already booked for these dates");
        }

        long nights = ChronoUnit.DAYS.between(dto.getCheckInDate(), dto.getCheckOutDate());
        BigDecimal totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));
        BigDecimal discountedPrice = applyLoyaltyDiscount(dto.getUserId(), totalPrice);
        Booking booking = bookingMapper.toEntity(dto);
        booking.setTotalPrice(discountedPrice);
        Booking saved = bookingRepository.save(booking);
        updateLoyaltyProgram(dto.getUserId(), discountedPrice);

        return bookingMapper.toDto(saved);
    }

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
            // Если что-то пошло не так, возвращаем полную цену
            return totalPrice;
        }
    }

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
            // Логируем ошибку, но не прерываем создание бронирования
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

    public BookingResponseDto updateBooking(UUID bookingId, @Valid BookingUpdateDto dto) {
        Booking booking = bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        if (booking.isDeleted()) {
            throw new RuntimeException("Cannot update deleted booking");
        }

        // Обновляем только те поля, которые были переданы
        if (dto.getCheckInDate() != null) {
            booking.setCheckInDate(dto.getCheckInDate());
        }

        if (dto.getCheckOutDate() != null) {
            booking.setCheckOutDate(dto.getCheckOutDate());
        }

        if (dto.getStatus() != null) {
            booking.setStatus(dto.getStatus());
        }

        // Если изменились даты, пересчитываем цену
        if (dto.getCheckInDate() != null || dto.getCheckOutDate() != null) {
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

    public void cancelBooking(UUID bookingId) {
        Booking booking = bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        if (booking.isDeleted()) {
            throw new RuntimeException("Booking is deleted");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    public void confirmBooking(UUID bookingId) {
        Booking booking = bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        if (booking.isDeleted()) {
            throw new RuntimeException("Booking is deleted");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
    }

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

    public List<BookingResponseDto> getBookingsByStatus(BookingStatus status) {
        return bookingRepository.findByStatusAndIsDeletedFalse(status)
                .stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }
}