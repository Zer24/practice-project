package org.example.service;

import lombok.AllArgsConstructor;
import org.example.domain.Hotel;
import org.example.dto.HotelCreateDto;
import org.example.dto.HotelResponseDto;
import org.example.dto.HotelUpdateDto;
import org.example.mapper.HotelMapper;
import org.example.repository.HotelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Validated
@AllArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;
    private final AuditService auditService;

    @Transactional
    public HotelResponseDto createHotel(@Valid HotelCreateDto dto) {
        Hotel hotel = hotelMapper.toEntity(dto);
        Hotel saved = hotelRepository.save(hotel);
        return hotelMapper.toDto(saved);
    }

    public List<HotelResponseDto> getAllHotels() {
        return hotelRepository.findByIsDeletedFalse()
                .stream()
                .map(HotelResponseDto::new)
                .collect(Collectors.toList());
    }

    public HotelResponseDto getHotel(UUID hotelId) {
        Hotel hotel = hotelRepository.findByHotelId(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + hotelId));

        if (hotel.isDeleted()) {
            throw new RuntimeException("Hotel is deleted");
        }

        return new HotelResponseDto(hotel);
    }

    @Transactional
    public HotelResponseDto updateHotel(UUID hotelId, @Valid HotelUpdateDto dto) {
        Hotel hotel = hotelRepository.findByHotelId(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + hotelId));

        if (hotel.isDeleted()) {
            throw new RuntimeException("Cannot update deleted hotel");
        }
        if (dto.name() != null && !dto.name().isEmpty()) {
            hotel.setName(dto.name());
        }
        if (dto.city() != null && !dto.city().isEmpty()) {
            hotel.setCity(dto.city());
        }
        if (dto.country() != null && !dto.country().isEmpty()) {
            hotel.setCountry(dto.country());
        }
        if (dto.address() != null && !dto.address().isEmpty()) {
            hotel.setAddress(dto.address());
        }
        if (dto.managerId() != null) {
            hotel.setManagerId(dto.managerId());
        }
        if (dto.rating() != null) {
            hotel.setRating(dto.rating());
        }
        Hotel updated = hotelRepository.save(hotel);
        return new HotelResponseDto(updated);
    }

    @Transactional
    public void softDeleteHotel(UUID hotelId, UUID performedBy) {
        Hotel hotel = hotelRepository.findByHotelId(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + hotelId));

        hotel.setDeleted(true);
        hotelRepository.save(hotel);

        auditService.logHotelDelete(hotelId, hotel.getName(), performedBy, true);
    }

    @Transactional
    public void restoreHotel(UUID hotelId, UUID performedBy) {
        Hotel hotel = hotelRepository.findByHotelId(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + hotelId));

        hotel.setDeleted(false);
        hotelRepository.save(hotel);

        auditService.logHotelDelete(hotelId, hotel.getName(), performedBy, false);
    }

    @Transactional
    public void hardDeleteHotel(UUID hotelId) {
        Hotel hotel = hotelRepository.findByHotelId(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + hotelId));

        hotelRepository.delete(hotel);
    }

    public List<HotelResponseDto> getHotelsByCity(String city) {
        return hotelRepository.findByCityAndIsDeletedFalse(city)
                .stream()
                .map(HotelResponseDto::new)
                .collect(Collectors.toList());
    }

    public List<HotelResponseDto> getHotelsByManagerId(UUID managerId) {
        return hotelRepository.findByManagerIdAndIsDeletedFalse(managerId)
                .stream()
                .map(HotelResponseDto::new)
                .collect(Collectors.toList());
    }
}