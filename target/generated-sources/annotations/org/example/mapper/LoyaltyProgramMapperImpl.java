package org.example.mapper;

import javax.annotation.processing.Generated;
import org.example.domain.LoyaltyProgram;
import org.example.dto.LoyaltyProgramCreateDto;
import org.example.dto.LoyaltyProgramResponseDto;
import org.example.dto.LoyaltyProgramUpdateDto;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-18T14:17:21+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class LoyaltyProgramMapperImpl implements LoyaltyProgramMapper {

    @Override
    public LoyaltyProgramResponseDto toDto(LoyaltyProgram loyaltyProgram) {
        if ( loyaltyProgram == null ) {
            return null;
        }

        LoyaltyProgramResponseDto loyaltyProgramResponseDto = new LoyaltyProgramResponseDto();

        loyaltyProgramResponseDto.setLoyaltyId( loyaltyProgram.getLoyaltyId() );
        loyaltyProgramResponseDto.setUserId( loyaltyProgram.getUserId() );
        loyaltyProgramResponseDto.setTotalPoints( loyaltyProgram.getTotalPoints() );
        loyaltyProgramResponseDto.setTier( loyaltyProgram.getTier() );
        loyaltyProgramResponseDto.setTotalSpent( loyaltyProgram.getTotalSpent() );

        return loyaltyProgramResponseDto;
    }

    @Override
    public LoyaltyProgram toEntity(LoyaltyProgramCreateDto dto) {
        if ( dto == null ) {
            return null;
        }

        LoyaltyProgram loyaltyProgram = new LoyaltyProgram();

        loyaltyProgram.setUserId( dto.getUserId() );
        loyaltyProgram.setTotalPoints( dto.getTotalPoints() );
        loyaltyProgram.setTier( dto.getTier() );

        generateLoyaltyId( loyaltyProgram );

        return loyaltyProgram;
    }

    @Override
    public void updateEntity(LoyaltyProgram loyaltyProgram, LoyaltyProgramUpdateDto dto) {
        if ( dto == null ) {
            return;
        }

        loyaltyProgram.setTotalPoints( dto.getTotalPoints() );
        loyaltyProgram.setTier( dto.getTier() );
        loyaltyProgram.setTotalSpent( dto.getTotalSpent() );

        generateLoyaltyId( loyaltyProgram );
    }
}
