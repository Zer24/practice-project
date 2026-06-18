package org.example.mapper;

import org.example.domain.User;
import org.example.dto.UserCreateDto;
import org.example.dto.UserResponseDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    User toEntity(UserCreateDto dto);

    @AfterMapping
    default void generateUserId(@MappingTarget User user) {
        user.setUserId(UUID.randomUUID());
    }
}