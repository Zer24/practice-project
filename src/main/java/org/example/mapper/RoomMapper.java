package org.example.mapper;

import org.example.domain.Room;
import org.example.domain.User;
import org.example.dto.RoomCreateDto;
import org.example.dto.RoomResponseDto;
import org.example.dto.RoomUpdateDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    RoomResponseDto toDto(Room room);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roomId", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Room toEntity(RoomCreateDto dto);

    @AfterMapping
    default void generateUserId(@MappingTarget Room room) {
        room.setRoomId(UUID.randomUUID());
    }
}