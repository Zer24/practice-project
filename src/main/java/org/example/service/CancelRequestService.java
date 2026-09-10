package org.example.service;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.domain.Booking;
import org.example.domain.CancelRequest;
import org.example.domain.enums.BookingStatus;
import org.example.domain.enums.CancelRequestStatus;
import org.example.dto.CancelRequestCreateDto;
import org.example.dto.CancelRequestDto;
import org.example.dto.CancelRequestUpdateDto;
import org.example.repository.BookingRepository;
import org.example.repository.CancelRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Validated
@AllArgsConstructor
public class CancelRequestService {

    private final CancelRequestRepository cancelRequestRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public CancelRequestDto createCancelRequest(@Valid CancelRequestCreateDto dto, UUID userId) {
        Booking booking = bookingRepository.findByBookingId(dto.bookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.isDeleted()) {
            throw new RuntimeException("Booking is deleted");
        }
        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("You can only request cancellation for your own bookings");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking is already cancelled");
        }
        if (cancelRequestRepository.existsByBookingIdAndStatus(dto.bookingId(), CancelRequestStatus.PENDING)) {
            throw new RuntimeException("A cancellation request is already pending for this booking");
        }

        CancelRequest cancelRequest = new CancelRequest(dto.bookingId(), userId, dto.reason());
        CancelRequest saved = cancelRequestRepository.save(cancelRequest);

        return convertToDto(saved);
    }

    public Page<CancelRequestDto> getAllCancelRequests(Pageable pageable) {
        return cancelRequestRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    public Page<CancelRequestDto> getCancelRequestsByUser(UUID userId, Pageable pageable) {
        return cancelRequestRepository.findByUserId(userId, pageable)
                .map(this::convertToDto);
    }

    @Transactional
    public CancelRequestDto processCancelRequest(UUID requestId, @Valid CancelRequestUpdateDto dto) {
        CancelRequest cancelRequest = cancelRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Cancel request not found"));

        if (cancelRequest.getStatus() != CancelRequestStatus.PENDING) {
            throw new RuntimeException("Request has already been processed");
        }
        cancelRequest.setStatus(dto.status());
        cancelRequest.setProcessedBy(dto.processedBy());
        cancelRequest.setProcessedAt(LocalDateTime.now());
        cancelRequestRepository.save(cancelRequest);

        if (dto.status() == CancelRequestStatus.APPROVED) {
            Booking booking = bookingRepository.findByBookingId(cancelRequest.getBookingId())
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
        }

        return convertToDto(cancelRequest);
    }

    private CancelRequestDto convertToDto(CancelRequest request) {
        return new CancelRequestDto(
                request.getRequestId(),
                request.getBookingId(),
                request.getUserId(),
                request.getReason(),
                request.getStatus().toString(),
                request.getCreatedAt(),
                request.getProcessedAt(),
                request.getProcessedBy()
        );
    }
    public Page<CancelRequestDto> getPendingCancelRequests(Pageable pageable) {
        return cancelRequestRepository.findByStatus(CancelRequestStatus.PENDING, pageable)
                .map(this::convertToDto);
    }
}