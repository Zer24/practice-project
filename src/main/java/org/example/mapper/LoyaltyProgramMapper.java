package org.example.mapper;

import org.example.domain.LoyaltyProgram;
import org.example.domain.User;
import org.example.dto.LoyaltyProgramCreateDto;
import org.example.dto.LoyaltyProgramResponseDto;
import org.example.dto.LoyaltyProgramUpdateDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface LoyaltyProgramMapper {

    LoyaltyProgramResponseDto toDto(LoyaltyProgram loyaltyProgram);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "loyaltyId", ignore = true)
    LoyaltyProgram toEntity(LoyaltyProgramCreateDto dto);

    @AfterMapping
    default void generateLoyaltyId(@MappingTarget LoyaltyProgram loyaltyProgram) {
        loyaltyProgram.setLoyaltyId(UUID.randomUUID());
    }
}