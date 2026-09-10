package org.example.presentation.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.BookingCreateDto;
import org.example.dto.BookingResponseDto;
import org.example.dto.BookingStatusUpdateDto;
import org.example.service.BookingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<BookingResponseDto> createBooking(@Valid @RequestBody BookingCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBooking(dto));
    }

    @GetMapping
    public ResponseEntity<Page<BookingResponseDto>> getBookings(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @PageableDefault() Pageable pageable) {
        UUID currentUserId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(bookingService.getBookings(currentUserId, status, fromDate, toDate, pageable));
    }

    @GetMapping("/{id}")
//    @PreAuthorize("@bookingService.canViewBooking(#id)")
    public ResponseEntity<BookingResponseDto> getBookingById(@PathVariable UUID id) {
        UUID currentUserId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(bookingService.getBookingById(id, currentUserId));
    }

    @PutMapping("/{id}")
//    @PreAuthorize("@bookingService.canUpdateBookingStatus(#id, #dto)")
    public ResponseEntity<Void> updateBookingStatus(
            @PathVariable UUID id,
            @Valid @RequestBody BookingStatusUpdateDto dto) {
        UUID currentUserId = securityUtils.getCurrentUserId();
        bookingService.updateBookingStatus(id, dto, currentUserId); //Проверка доступа в сервисе
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> softDeleteBooking(@PathVariable UUID id) {
        UUID currentUserId = securityUtils.getCurrentUserId();
        bookingService.softDeleteBooking(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}