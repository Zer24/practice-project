package org.example.presentation.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.HotelCreateDto;
import org.example.dto.HotelResponseDto;
import org.example.dto.HotelUpdateDto;
import org.example.service.HotelService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<Page<HotelResponseDto>> getAllHotels(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double maxRating,
            @RequestParam(required = false) String sort,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(hotelService.getAllHotels(city, country, minRating, maxRating, sort, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelResponseDto> getHotelById(@PathVariable UUID id) {
        return ResponseEntity.ok(hotelService.getHotelById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HOTEL_MANAGER')")
    public ResponseEntity<HotelResponseDto> createHotel(@Valid @RequestBody HotelCreateDto dto) {
        UUID currentUserId = securityUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(hotelService.createHotel(dto, currentUserId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOTEL_MANAGER')")
    public ResponseEntity<HotelResponseDto> updateHotel(
            @PathVariable UUID id,
            @Valid @RequestBody HotelUpdateDto dto) {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(hotelService.updateHotel(id, dto, userId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteHotel(@PathVariable UUID id) {
        UUID currentUserId = securityUtils.getCurrentUserId();
        hotelService.softDeleteHotel(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}