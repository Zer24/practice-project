package org.example.service;

import org.example.domain.Room;
import org.example.domain.RoomType;
import org.example.dto.RoomCreateDto;
import org.example.dto.RoomResponseDto;
import org.example.dto.RoomUpdateDto;
import org.example.mapper.RoomMapper;
import org.example.repository.RoomRepository;
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
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    public RoomService(RoomRepository roomRepository, RoomMapper roomMapper) {
        this.roomRepository = roomRepository;
        this.roomMapper = roomMapper;
    }
    public RoomResponseDto createRoom(@Valid RoomCreateDto dto) {
        Room room = roomMapper.toEntity(dto);
        Room saved = roomRepository.save(room);
        return roomMapper.toDto(saved);
    }
    public List<RoomResponseDto> getAllRooms() {
        return roomRepository.findByIsDeletedFalse()
                .stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
    }
    public RoomResponseDto getRoom(UUID roomId) {
        Room room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        if (room.isDeleted()) {
            throw new RuntimeException("Room is deleted");
        }

        return roomMapper.toDto(room);
    }
    public RoomResponseDto updateRoom(UUID roomId, @Valid RoomUpdateDto dto) {
        Room room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        if (room.isDeleted()) {
            throw new RuntimeException("Cannot update deleted room");
        }

        // Обновляем только те поля, которые были переданы (не null)
        if (dto.getRoomType() != null) {
            room.setRoomType(dto.getRoomType());
        }

        if (dto.getPricePerNight() != null) {
            room.setPricePerNight(dto.getPricePerNight());
        }

        if (dto.getCapacity() != null) {
            room.setCapacity(dto.getCapacity());
        }

        if (dto.getDescription() != null && !dto.getDescription().isEmpty()) {
            room.setDescription(dto.getDescription());
        }

        if (dto.getAmenities() != null) {
            room.setAmenities(dto.getAmenities());
        }

        if (dto.getArea() != null) {
            room.setArea(dto.getArea());
        }

        Room updated = roomRepository.save(room);
        return roomMapper.toDto(updated);
    }
    public void softDeleteRoom(UUID roomId) {
        Room room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        room.setDeleted(true);
        roomRepository.save(room);
    }
    public void restoreRoom(UUID roomId) {
        Room room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        room.setDeleted(false);
        roomRepository.save(room);
    }
    public void hardDeleteRoom(UUID roomId) {
        Room room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        roomRepository.delete(room);
    }
    public List<RoomResponseDto> getRoomsByHotel(UUID hotelId) {
        return roomRepository.findByHotelIdAndIsDeletedFalse(hotelId)
                .stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
    }
    public List<RoomResponseDto> getRoomsByType(RoomType roomType) {
        return roomRepository.findByRoomTypeAndIsDeletedFalse(roomType)
                .stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
    }
}