package org.example.mapper;

import java.math.BigDecimal;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.example.domain.LoyaltyProgram;
import org.example.domain.enums.LoyaltyTier;
import org.example.dto.LoyaltyProgramCreateDto;
import org.example.dto.LoyaltyProgramResponseDto;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-19T23:11:03+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class LoyaltyProgramMapperImpl implements LoyaltyProgramMapper {

    @Override
    public LoyaltyProgramResponseDto toDto(LoyaltyProgram loyaltyProgram) {
        if ( loyaltyProgram == null ) {
            return null;
        }

        UUID loyaltyId = null;
        UUID userId = null;
        Integer totalPoints = null;
        LoyaltyTier tier = null;
        BigDecimal totalSpent = null;

        loyaltyId = loyaltyProgram.getLoyaltyId();
        userId = loyaltyProgram.getUserId();
        totalPoints = loyaltyProgram.getTotalPoints();
        tier = loyaltyProgram.getTier();
        totalSpent = loyaltyProgram.getTotalSpent();

        LoyaltyProgramResponseDto loyaltyProgramResponseDto = new LoyaltyProgramResponseDto( loyaltyId, userId, totalPoints, tier, totalSpent );

        return loyaltyProgramResponseDto;
    }

    @Override
    public LoyaltyProgram toEntity(LoyaltyProgramCreateDto dto) {
        if ( dto == null ) {
            return null;
        }

        LoyaltyProgram loyaltyProgram = new LoyaltyProgram();

        loyaltyProgram.setUserId( dto.userId() );
        loyaltyProgram.setTotalPoints( dto.totalPoints() );
        loyaltyProgram.setTier( dto.tier() );

        generateLoyaltyId( loyaltyProgram );

        return loyaltyProgram;
    }
}
