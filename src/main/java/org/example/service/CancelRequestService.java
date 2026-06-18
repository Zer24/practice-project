package org.example.service;

import org.example.domain.Booking;
import org.example.domain.BookingStatus;
import org.example.domain.CancelRequest;
import org.example.domain.CancelRequestStatus;
import org.example.dto.CancelRequestCreateDto;
import org.example.dto.CancelRequestDto;
import org.example.dto.CancelRequestUpdateDto;
import org.example.repository.BookingRepository;
import org.example.repository.CancelRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Validated
@Transactional
public class CancelRequestService {

    private final CancelRequestRepository cancelRequestRepository;
    private final BookingRepository bookingRepository;

    public CancelRequestService(CancelRequestRepository cancelRequestRepository,
                                BookingRepository bookingRepository) {
        this.cancelRequestRepository = cancelRequestRepository;
        this.bookingRepository = bookingRepository;
    }
    public CancelRequestDto createCancelRequest(@Valid CancelRequestCreateDto dto, UUID userId) {
        Booking booking = bookingRepository.findByBookingId(dto.getBookingId())
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
        if (cancelRequestRepository.existsByBookingIdAndStatus(dto.getBookingId(), CancelRequestStatus.PENDING)) {
            throw new RuntimeException("A cancellation request is already pending for this booking");
        }

        CancelRequest cancelRequest = new CancelRequest(dto.getBookingId(), userId, dto.getReason());
        CancelRequest saved = cancelRequestRepository.save(cancelRequest);

        return convertToDto(saved);
    }
    public List<CancelRequestDto> getAllCancelRequests() {
        return cancelRequestRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    public CancelRequestDto getCancelRequest(UUID requestId) {
        CancelRequest cancelRequest = cancelRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Cancel request not found"));
        return convertToDto(cancelRequest);
    }
    public List<CancelRequestDto> getCancelRequestsByUser(UUID userId) {
        return cancelRequestRepository.findByUserId(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    public List<CancelRequestDto> getCancelRequestsByStatus(CancelRequestStatus status) {
        return cancelRequestRepository.findByStatus(status)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    public CancelRequestDto processCancelRequest(UUID requestId, @Valid CancelRequestUpdateDto dto) {
        CancelRequest cancelRequest = cancelRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Cancel request not found"));

        if (cancelRequest.getStatus() != CancelRequestStatus.PENDING) {
            throw new RuntimeException("Request has already been processed");
        }
        cancelRequest.setStatus(dto.getStatus());
        cancelRequest.setProcessedBy(dto.getProcessedBy());
        cancelRequest.setProcessedAt(LocalDateTime.now());
        cancelRequestRepository.save(cancelRequest);

        // Если запрос одобрен, отменяем бронирование
        if (dto.getStatus() == CancelRequestStatus.APPROVED) {
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
}