package org.example.service;

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
@Transactional
public class HotelService {

    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;
    private final AuditService auditService;

    public HotelService(HotelRepository hotelRepository,
                        HotelMapper hotelMapper,
                        AuditService auditService) {
        this.hotelRepository = hotelRepository;
        this.hotelMapper = hotelMapper;
        this.auditService = auditService;
    }

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

    public HotelResponseDto updateHotel(UUID hotelId, @Valid HotelUpdateDto dto) {
        Hotel hotel = hotelRepository.findByHotelId(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + hotelId));

        if (hotel.isDeleted()) {
            throw new RuntimeException("Cannot update deleted hotel");
        }
        if (dto.getName() != null && !dto.getName().isEmpty()) {
            hotel.setName(dto.getName());
        }
        if (dto.getCity() != null && !dto.getCity().isEmpty()) {
            hotel.setCity(dto.getCity());
        }
        if (dto.getCountry() != null && !dto.getCountry().isEmpty()) {
            hotel.setCountry(dto.getCountry());
        }
        if (dto.getAddress() != null && !dto.getAddress().isEmpty()) {
            hotel.setAddress(dto.getAddress());
        }
        if (dto.getManagerId() != null) {
            hotel.setManagerId(dto.getManagerId());
        }
        if (dto.getRating() != null) {
            hotel.setRating(dto.getRating());
        }
        Hotel updated = hotelRepository.save(hotel);
        return new HotelResponseDto(updated);
    }

    public void softDeleteHotel(UUID hotelId, UUID performedBy) {
        Hotel hotel = hotelRepository.findByHotelId(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + hotelId));

        hotel.setDeleted(true);
        hotelRepository.save(hotel);

        auditService.logHotelDelete(hotelId, hotel.getName(), performedBy, true);
    }

    public void restoreHotel(UUID hotelId, UUID performedBy) {
        Hotel hotel = hotelRepository.findByHotelId(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + hotelId));

        hotel.setDeleted(false);
        hotelRepository.save(hotel);

        auditService.logHotelDelete(hotelId, hotel.getName(), performedBy, false);
    }

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