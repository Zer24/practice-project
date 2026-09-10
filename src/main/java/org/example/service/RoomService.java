package org.example.service;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.Booking;
import org.example.domain.Room;
import org.example.domain.enums.BookingStatus;
import org.example.domain.enums.RoomType;
import org.example.dto.RoomCreateDto;
import org.example.dto.RoomResponseDto;
import org.example.dto.RoomUpdateDto;
import org.example.mapper.RoomMapper;
import org.example.repository.BookingRepository;
import org.example.repository.RoomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Validated
@AllArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    private final BookingRepository bookingRepository;

    @Transactional
    public RoomResponseDto createRoom(UUID hotelId, @Valid RoomCreateDto dto) {
        Room room = roomMapper.toEntity(dto);
        room.setHotelId(hotelId);
        Room saved = roomRepository.save(room);
        log.info("Created room with id: {} in hotel: {}", saved.getRoomId(), hotelId);
        return roomMapper.toDto(saved);
    }
    public Page<RoomResponseDto> getRoomsByHotel(
            UUID hotelId,
            String roomType,
            Integer capacity,
            Double minPrice,
            Double maxPrice,
            String amenities,
            String sort,
            Pageable pageable) {

        Pageable sortedPageable = createSortedPageable(pageable, sort);

        BigDecimal minPriceBig = minPrice != null ? BigDecimal.valueOf(minPrice) : null;
        BigDecimal maxPriceBig = maxPrice != null ? BigDecimal.valueOf(maxPrice) : null;

        Page<Room> rooms = roomRepository.findWithFilters(
                hotelId, roomType, capacity, null,
                minPriceBig, maxPriceBig, amenities, sortedPageable
        );

        return rooms.map(roomMapper::toDto);
    }

    public RoomResponseDto getRoomById(UUID hotelId, UUID roomId) {
        Room room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        if (room.isDeleted()) {
            throw new RuntimeException("Room is deleted");
        }

        if (!room.getHotelId().equals(hotelId)) {
            throw new RuntimeException("Room does not belong to this hotel");
        }

        return roomMapper.toDto(room);
    }

    @Transactional
    public RoomResponseDto updateRoom(UUID hotelId, UUID roomId, @Valid RoomUpdateDto dto, UUID currentUserId) {
        Room room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        if (room.isDeleted()) {
            throw new RuntimeException("Cannot update deleted room");
        }

        if (!room.getHotelId().equals(hotelId)) {
            throw new RuntimeException("Room does not belong to this hotel");
        }

        updateRoomFields(room, dto);

        Room updated = roomRepository.save(room);
        log.info("Updated room with id: {} in hotel: {}", updated.getRoomId(), hotelId);
        return roomMapper.toDto(updated);
    }

    private void updateRoomFields(Room room, RoomUpdateDto dto) {
        if (dto.roomType() != null) {
            room.setRoomType(dto.roomType());
        }
        if (dto.pricePerNight() != null) {
            room.setPricePerNight(dto.pricePerNight());
        }
        if (dto.capacity() != null) {
            room.setCapacity(dto.capacity());
        }
        if (dto.description() != null && !dto.description().isEmpty()) {
            room.setDescription(dto.description());
        }
        if (dto.amenities() != null) {
            room.setAmenities(dto.amenities());
        }
        if (dto.area() != null) {
            room.setArea(dto.area());
        }
    }

    @Transactional
    public void softDeleteRoom(UUID hotelId, UUID roomId, UUID currentUserId) {
        Room room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        if (!room.getHotelId().equals(hotelId)) {
            throw new RuntimeException("Room does not belong to this hotel");
        }

        room.setDeleted(true);
        roomRepository.save(room);
        log.info("Soft deleted room with id: {} in hotel: {}", roomId, hotelId);
    }

    @Transactional
    public void restoreRoom(UUID roomId) {
        Room room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        room.setDeleted(false);
        roomRepository.save(room);
        log.info("Restored room with id: {}", roomId);
    }

    public List<RoomResponseDto> getRoomsByType(RoomType roomType) {
        return roomRepository.findByRoomTypeAndIsDeletedFalse(roomType)
                .stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
    }
    public List<RoomResponseDto> searchAvailableRooms(UUID hotelId, String checkInStr, String checkOutStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate checkIn = LocalDate.parse(checkInStr, formatter);
        LocalDate checkOut = LocalDate.parse(checkOutStr, formatter);

        List<Room> allRooms = roomRepository.findActiveByHotelId(hotelId);

        List<UUID> bookedRoomIds = bookingRepository.findActiveBookingsByDateRange(
                        checkIn, checkOut,
                        List.of(BookingStatus.CREATED, BookingStatus.CONFIRMED)
                ).stream()
                .map(Booking::getRoomId)
                .collect(Collectors.toList());

        return allRooms.stream()
                .filter(room -> !bookedRoomIds.contains(room.getRoomId()))
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
    }

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
    private boolean isValidSortField(String field) {
        return Set.of("roomType", "pricePerNight", "capacity", "area").contains(field.toLowerCase());
    }
}