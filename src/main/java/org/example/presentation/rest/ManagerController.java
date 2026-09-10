package org.example.presentation.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.AuditLogDto;
import org.example.dto.CancelRequestDto;
import org.example.dto.CancelRequestUpdateDto;
import org.example.service.AuditService;
import org.example.service.CancelRequestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'ROLE_ADMIN')")
public class ManagerController {

    private final CancelRequestService cancelRequestService;
    private final AuditService auditService;
    private final SecurityUtils securityUtils;

    @GetMapping("/cancel-requests")
    public ResponseEntity<Page<CancelRequestDto>> getAllCancelRequests(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(cancelRequestService.getAllCancelRequests(pageable));
    }

    @GetMapping("/cancel-requests/pending")
    public ResponseEntity<Page<CancelRequestDto>> getPendingCancelRequests(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(cancelRequestService.getPendingCancelRequests(pageable));
    }

    @PutMapping("/cancel-requests/{requestId}")
    public ResponseEntity<CancelRequestDto> processCancelRequest(
            @PathVariable UUID requestId,
            @Valid @RequestBody CancelRequestUpdateDto dto) {
        return ResponseEntity.ok(cancelRequestService.processCancelRequest(requestId, dto));
    }

    @GetMapping("/audit")
    public ResponseEntity<Page<AuditLogDto>> getMyAuditLogs(
            @PageableDefault(size = 10) Pageable pageable) {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(auditService.getAuditLogsByUser(userId, pageable));
    }
}