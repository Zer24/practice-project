package org.example.service;

import lombok.AllArgsConstructor;
import org.example.config.BusinessProperties;
import org.example.domain.LoyaltyProgram;
import org.example.domain.enums.LoyaltyTier;
import org.example.dto.LoyaltyProgramCreateDto;
import org.example.dto.LoyaltyProgramResponseDto;
import org.example.dto.LoyaltyProgramUpdateDto;
import org.example.mapper.LoyaltyProgramMapper;
import org.example.repository.LoyaltyProgramRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Validated
@AllArgsConstructor
public class LoyaltyProgramService {

    private final LoyaltyProgramRepository loyaltyProgramRepository;
    private final LoyaltyProgramMapper loyaltyProgramMapper;
    private final BusinessProperties properties;

    @Transactional
    public LoyaltyProgramResponseDto createLoyaltyProgram(@Valid LoyaltyProgramCreateDto dto) {
        if (loyaltyProgramRepository.existsByUserId(dto.userId())) {
            throw new RuntimeException("Loyalty program already exists for user: " + dto.userId());
        }

        LoyaltyProgram loyaltyProgram = loyaltyProgramMapper.toEntity(dto);
        loyaltyProgram.setTotalSpent(BigDecimal.ZERO);

        LoyaltyProgram saved = loyaltyProgramRepository.save(loyaltyProgram);
        return loyaltyProgramMapper.toDto(saved);
    }
    public List<LoyaltyProgramResponseDto> getAllLoyaltyPrograms() {
        return loyaltyProgramRepository.findAll()
                .stream()
                .map(loyaltyProgramMapper::toDto)
                .collect(Collectors.toList());
    }
    public LoyaltyProgramResponseDto getLoyaltyProgram(UUID loyaltyId) {
        LoyaltyProgram loyaltyProgram = loyaltyProgramRepository.findByLoyaltyId(loyaltyId)
                .orElseThrow(() -> new RuntimeException("Loyalty program not found with id: " + loyaltyId));

        return loyaltyProgramMapper.toDto(loyaltyProgram);
    }
    public LoyaltyProgramResponseDto getLoyaltyProgramByUser(UUID userId) {
        LoyaltyProgram loyaltyProgram = loyaltyProgramRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Loyalty program not found for user: " + userId));

        return loyaltyProgramMapper.toDto(loyaltyProgram);
    }

    @Transactional
    public LoyaltyProgramResponseDto updateLoyaltyProgram(UUID loyaltyId, @Valid LoyaltyProgramUpdateDto dto) {
        LoyaltyProgram loyaltyProgram = loyaltyProgramRepository.findByLoyaltyId(loyaltyId)
                .orElseThrow(() -> new RuntimeException("Loyalty program not found with id: " + loyaltyId));

        if (dto.totalPoints() != null) {
            loyaltyProgram.setTotalPoints(dto.totalPoints());
            updateTierByPoints(loyaltyProgram);
        }

        if (dto.tier() != null) {
            loyaltyProgram.setTier(dto.tier());
        }

        if (dto.totalSpent() != null) {
            loyaltyProgram.setTotalSpent(dto.totalSpent());
        }

        LoyaltyProgram updated = loyaltyProgramRepository.save(loyaltyProgram);
        return loyaltyProgramMapper.toDto(updated);
    }

    @Transactional
    public LoyaltyProgramResponseDto addPoints(UUID userId, Integer points) {
        LoyaltyProgram loyaltyProgram = loyaltyProgramRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Loyalty program not found for user: " + userId));

        int newTotalPoints = loyaltyProgram.getTotalPoints() + points;
        loyaltyProgram.setTotalPoints(newTotalPoints);

        updateTierByPoints(loyaltyProgram);

        LoyaltyProgram updated = loyaltyProgramRepository.save(loyaltyProgram);
        return loyaltyProgramMapper.toDto(updated);
    }
    private void updateTierByPoints(LoyaltyProgram loyaltyProgram) {
        int points = loyaltyProgram.getTotalPoints();
        Map<String, Integer> tiers = properties.getLoyalty().getTiers();

        if (points >= tiers.get("platinum")) {
            loyaltyProgram.setTier(LoyaltyTier.PLATINUM);
        } else if (points >= tiers.get("gold")) {
            loyaltyProgram.setTier(LoyaltyTier.GOLD);
        } else if (points >= tiers.get("silver")) {
            loyaltyProgram.setTier(LoyaltyTier.SILVER);
        } else {
            loyaltyProgram.setTier(LoyaltyTier.BRONZE);
        }
    }

    @Transactional
    public LoyaltyProgramResponseDto addSpentAmount(UUID userId, BigDecimal amount) {
        LoyaltyProgram loyaltyProgram = loyaltyProgramRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Loyalty program not found for user: " + userId));

        BigDecimal newTotalSpent = loyaltyProgram.getTotalSpent().add(amount);
        loyaltyProgram.setTotalSpent(newTotalSpent);

        int pointsPerHundred = properties.getLoyalty().getPointsPer100Dollars();
        int pointsToAdd = amount.divide(BigDecimal.valueOf(100)).intValue() * pointsPerHundred;

        if (pointsToAdd > 0) {
            loyaltyProgram.setTotalPoints(loyaltyProgram.getTotalPoints() + pointsToAdd);
            updateTierByPoints(loyaltyProgram);
        }

        LoyaltyProgram updated = loyaltyProgramRepository.save(loyaltyProgram);
        return loyaltyProgramMapper.toDto(updated);
    }
    public List<LoyaltyProgramResponseDto> getLoyaltyProgramsByTier(LoyaltyTier tier) {
        return loyaltyProgramRepository.findByTier(tier)
                .stream()
                .map(loyaltyProgramMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public LoyaltyProgramResponseDto redeemPoints(UUID userId, Integer pointsToRedeem) {
        LoyaltyProgram loyaltyProgram = loyaltyProgramRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Loyalty program not found for user: " + userId));

        if (loyaltyProgram.getTotalPoints() < pointsToRedeem) {
            throw new RuntimeException("Insufficient points. Available: " + loyaltyProgram.getTotalPoints());
        }

        int newTotalPoints = loyaltyProgram.getTotalPoints() - pointsToRedeem;
        loyaltyProgram.setTotalPoints(newTotalPoints);

        updateTierByPoints(loyaltyProgram);

        LoyaltyProgram updated = loyaltyProgramRepository.save(loyaltyProgram);
        return loyaltyProgramMapper.toDto(updated);
    }

    @Transactional
    public void deleteLoyaltyProgram(UUID loyaltyId) {
        LoyaltyProgram loyaltyProgram = loyaltyProgramRepository.findByLoyaltyId(loyaltyId)
                .orElseThrow(() -> new RuntimeException("Loyalty program not found with id: " + loyaltyId));

        loyaltyProgramRepository.delete(loyaltyProgram);
    }
}