package org.example.presentation.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.RoomCreateDto;
import org.example.dto.RoomResponseDto;
import org.example.dto.RoomUpdateDto;
import org.example.service.RoomService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/hotels/{hotelId}/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<Page<RoomResponseDto>> getRoomsByHotel(
            @PathVariable UUID hotelId,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String amenities,
            @RequestParam(required = false) String sort,
            @PageableDefault() Pageable pageable) {
        return ResponseEntity.ok(roomService.getRoomsByHotel(
                hotelId, roomType, capacity, minPrice, maxPrice, amenities, sort, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomResponseDto> getRoomById(
            @PathVariable UUID hotelId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(roomService.getRoomById(hotelId, id));
    }

    @GetMapping("/available")
    public ResponseEntity<List<RoomResponseDto>> getAvailableRooms(
            @PathVariable UUID hotelId,
            @RequestParam String checkIn,
            @RequestParam String checkOut) {
        return ResponseEntity.ok(roomService.searchAvailableRooms(hotelId, checkIn, checkOut));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HOTEL_MANAGER')")
    public ResponseEntity<RoomResponseDto> createRoom(
            @PathVariable UUID hotelId,
            @Valid @RequestBody RoomCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roomService.createRoom(hotelId, dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOTEL_MANAGER')")
    public ResponseEntity<RoomResponseDto> updateRoom(
            @PathVariable UUID hotelId,
            @PathVariable UUID id,
            @Valid @RequestBody RoomUpdateDto dto) {
        UUID currentUserId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(roomService.updateRoom(hotelId, id, dto, currentUserId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOTEL_MANAGER')")
    public ResponseEntity<Void> deleteRoom(
            @PathVariable UUID hotelId,
            @PathVariable UUID id) {
        UUID currentUserId = securityUtils.getCurrentUserId();
        roomService.softDeleteRoom(hotelId, id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}