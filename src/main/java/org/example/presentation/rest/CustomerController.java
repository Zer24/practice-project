package org.example.presentation.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.CancelRequestCreateDto;
import org.example.dto.CancelRequestDto;
import org.example.dto.LoyaltyProgramResponseDto;
import org.example.service.CancelRequestService;
import org.example.service.LoyaltyProgramService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerController {

    private final LoyaltyProgramService loyaltyProgramService;
    private final CancelRequestService cancelRequestService;
    private final SecurityUtils securityUtils;

    @GetMapping("/loyalty/me")
    public ResponseEntity<LoyaltyProgramResponseDto> getMyLoyalty() {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(loyaltyProgramService.getLoyaltyProgramByUser(userId));
    }

    @PostMapping("/cancel-requests")
    public ResponseEntity<CancelRequestDto> requestCancellation(
            @Valid @RequestBody CancelRequestCreateDto dto) {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cancelRequestService.createCancelRequest(dto, userId));
    }

    @GetMapping("/cancel-requests")
    public ResponseEntity<Page<CancelRequestDto>> getMyCancelRequests(
            @PageableDefault(size = 10) Pageable pageable) {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(cancelRequestService.getCancelRequestsByUser(userId, pageable));
    }
}