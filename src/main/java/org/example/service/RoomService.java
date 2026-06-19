package org.example.service;

import lombok.AllArgsConstructor;
import org.example.domain.Room;
import org.example.domain.enums.RoomType;
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
@AllArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    @Transactional
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

    @Transactional
    public RoomResponseDto updateRoom(UUID roomId, @Valid RoomUpdateDto dto) {
        Room room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        if (room.isDeleted()) {
            throw new RuntimeException("Cannot update deleted room");
        }

        // Обновляем только те поля, которые были переданы (не null)
        if (dto.roomType() != null) {
            room.setRoomType(dto.roomType());}
        if (dto.pricePerNight() != null) {
            room.setPricePerNight(dto.pricePerNight());}
        if (dto.capacity() != null) {
            room.setCapacity(dto.capacity());}
        if (dto.description() != null && !dto.description().isEmpty()) {
            room.setDescription(dto.description());}
        if (dto.amenities() != null) {
            room.setAmenities(dto.amenities());}
        if (dto.area() != null) {
            room.setArea(dto.area());}

        Room updated = roomRepository.save(room);
        return roomMapper.toDto(updated);
    }

    @Transactional
    public void softDeleteRoom(UUID roomId) {
        Room room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        room.setDeleted(true);
        roomRepository.save(room);
    }

    @Transactional
    public void restoreRoom(UUID roomId) {
        Room room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        room.setDeleted(false);
        roomRepository.save(room);
    }

    @Transactional
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