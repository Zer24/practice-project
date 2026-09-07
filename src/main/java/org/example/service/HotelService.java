// HotelService.java
package org.example.service;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.Hotel;
import org.example.domain.User;
import org.example.domain.enums.Role;
import org.example.dto.HotelCreateDto;
import org.example.dto.HotelResponseDto;
import org.example.dto.HotelUpdateDto;
import org.example.mapper.HotelMapper;
import org.example.repository.HotelRepository;
import org.example.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Validated
@AllArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;
    private final AuditService auditService;
    private final UserRepository userRepository;

    @Transactional
    public HotelResponseDto createHotel(@Valid HotelCreateDto dto, UUID currentUserId) {
        Hotel hotel = hotelMapper.toEntity(dto);
        hotel.setManagerId(currentUserId);
        Hotel saved = hotelRepository.save(hotel);
        log.info("Created hotel with id: {}", saved.getHotelId());
        return hotelMapper.toDto(saved);
    }

    public Page<HotelResponseDto> getAllHotels(
            String city,
            String country,
            Double minRating,
            Double maxRating,
            String sort,
            Pageable pageable) {

        // Создаем сортировку из строки sort
        Pageable sortedPageable = createSortedPageable(pageable, sort);

        // Используем кастомный репозиторий с динамическими фильтрами
        Page<Hotel> hotels = hotelRepository.findWithFilters(
                city, country, minRating, maxRating, sortedPageable
        );

        return hotels.map(HotelResponseDto::new);
    }

    /**
     * Получение всех отелей без пагинации с фильтрами
     */
    public List<HotelResponseDto> getAllHotelsWithFilters(
            String city,
            String country,
            Double minRating,
            Double maxRating) {

        List<Hotel> hotels = hotelRepository.findWithFilters(
                city, country, minRating, maxRating
        );

        return hotels.stream()
                .map(HotelResponseDto::new)
                .collect(Collectors.toList());
    }

    public Page<HotelResponseDto> getAllHotels(Pageable pageable) {
        return getAllHotels(null, null, null, null, null, pageable);
    }

    public List<HotelResponseDto> getAllHotels() {
        return getAllHotelsWithFilters(null, null, null, null);
    }

    public Page<HotelResponseDto> getHotelsByCity(String city, Pageable pageable) {
        return getAllHotels(city, null, null, null, null, pageable);
    }

    public List<HotelResponseDto> getHotelsByCity(String city) {
        return getAllHotelsWithFilters(city, null, null, null);
    }

    public Page<HotelResponseDto> getHotelsByManagerId(UUID managerId, Pageable pageable) {
        return hotelRepository.findByManagerIdAndIsDeletedFalse(managerId, pageable)
                .map(HotelResponseDto::new);
    }

    public List<HotelResponseDto> getHotelsByManagerId(UUID managerId) {
        return hotelRepository.findByManagerIdAndIsDeletedFalse(managerId)
                .stream()
                .map(HotelResponseDto::new)
                .collect(Collectors.toList());
    }

    public HotelResponseDto getHotelById(UUID hotelId) {
        Hotel hotel = hotelRepository.findByHotelId(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + hotelId));

        if (hotel.isDeleted()) {
            throw new RuntimeException("Hotel is deleted");
        }

        return new HotelResponseDto(hotel);
    }

    @Transactional
    public HotelResponseDto updateHotel(UUID hotelId, @Valid HotelUpdateDto dto, UUID userId) {
        Hotel hotel = hotelRepository.findByHotelId(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + hotelId));

        if (hotel.isDeleted()) {
            throw new RuntimeException("Cannot update deleted hotel");
        }
        User user = userRepository.findByUserId(userId).orElseThrow();
        if(user.getRole() != Role.ADMIN){
            if(user.getRole() != Role.MANAGER || !hotel.getManagerId().equals(userId)){
                throw new RuntimeException("You do not have permissions to update this hotel");
            }
        }

        // Обновляем только переданные поля
        updateHotelFields(hotel, dto);

        Hotel updated = hotelRepository.save(hotel);
        log.info("Updated hotel with id: {}", updated.getHotelId());
        return new HotelResponseDto(updated);
    }

    private void updateHotelFields(Hotel hotel, HotelUpdateDto dto) {
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
    }

    @Transactional
    public void softDeleteHotel(UUID hotelId, UUID performedBy) {
        Hotel hotel = hotelRepository.findByHotelId(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + hotelId));

        hotel.setDeleted(true);
        hotelRepository.save(hotel);

        auditService.logHotelDelete(hotelId, hotel.getName(), performedBy, true);
        log.info("Soft deleted hotel with id: {}", hotelId);
    }

    @Transactional
    public void restoreHotel(UUID hotelId, UUID performedBy) {
        Hotel hotel = hotelRepository.findByHotelId(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + hotelId));

        hotel.setDeleted(false);
        hotelRepository.save(hotel);

        auditService.logHotelDelete(hotelId, hotel.getName(), performedBy, false);
        log.info("Restored hotel with id: {}", hotelId);
    }

    @Transactional
    public void hardDeleteHotel(UUID hotelId) {
        Hotel hotel = hotelRepository.findByHotelId(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + hotelId));

        hotelRepository.delete(hotel);
        log.info("Hard deleted hotel with id: {}", hotelId);
    }

    /**
     * Создает Pageable с сортировкой на основе строки параметра sort
     */
    private Pageable createSortedPageable(Pageable pageable, String sort) {
        if (sort == null || sort.trim().isEmpty()) {
            return pageable;
        }

        try {
            String[] sortParts = sort.split(",");
            String field = sortParts[0].trim();
            String direction = sortParts.length > 1 ? sortParts[1].trim() : "asc";

            if (!isValidSortField(field)) {
                log.warn("Invalid sort field: {}, using default sorting", field);
                return pageable;
            }

            Sort.Direction sortDirection = direction.equalsIgnoreCase("desc")
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;

            return PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(sortDirection, field)
            );
        } catch (Exception e) {
            log.error("Error creating sort from string: {}, using default", sort, e);
            return pageable;
        }
    }

    /**
     * Проверяет, является ли поле допустимым для сортировки
     */
    private boolean isValidSortField(String field) {
        return Set.of("rating", "name", "city", "country").contains(field.toLowerCase());
    }
}