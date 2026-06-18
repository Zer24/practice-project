package org.example.service;

import org.example.domain.LoyaltyProgram;
import org.example.domain.LoyaltyTier;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Validated
@Transactional
public class LoyaltyProgramService {

    private final LoyaltyProgramRepository loyaltyProgramRepository;
    private final LoyaltyProgramMapper loyaltyProgramMapper;

    public LoyaltyProgramService(LoyaltyProgramRepository loyaltyProgramRepository,
                                 LoyaltyProgramMapper loyaltyProgramMapper) {
        this.loyaltyProgramRepository = loyaltyProgramRepository;
        this.loyaltyProgramMapper = loyaltyProgramMapper;
    }
    public LoyaltyProgramResponseDto createLoyaltyProgram(@Valid LoyaltyProgramCreateDto dto) {
        if (loyaltyProgramRepository.existsByUserId(dto.getUserId())) {
            throw new RuntimeException("Loyalty program already exists for user: " + dto.getUserId());
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
    public LoyaltyProgramResponseDto updateLoyaltyProgram(UUID loyaltyId, @Valid LoyaltyProgramUpdateDto dto) {
        LoyaltyProgram loyaltyProgram = loyaltyProgramRepository.findByLoyaltyId(loyaltyId)
                .orElseThrow(() -> new RuntimeException("Loyalty program not found with id: " + loyaltyId));

        // Обновляем только те поля, которые были переданы
        if (dto.getTotalPoints() != null) {
            loyaltyProgram.setTotalPoints(dto.getTotalPoints());
            // Если изменились баллы, обновляем уровень
            updateTierByPoints(loyaltyProgram);
        }

        if (dto.getTier() != null) {
            loyaltyProgram.setTier(dto.getTier());
        }

        if (dto.getTotalSpent() != null) {
            loyaltyProgram.setTotalSpent(dto.getTotalSpent());
        }

        LoyaltyProgram updated = loyaltyProgramRepository.save(loyaltyProgram);
        return loyaltyProgramMapper.toDto(updated);
    }
    public LoyaltyProgramResponseDto addPoints(UUID userId, Integer points) {
        LoyaltyProgram loyaltyProgram = loyaltyProgramRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Loyalty program not found for user: " + userId));

        int newTotalPoints = loyaltyProgram.getTotalPoints() + points;
        loyaltyProgram.setTotalPoints(newTotalPoints);

        updateTierByPoints(loyaltyProgram);

        LoyaltyProgram updated = loyaltyProgramRepository.save(loyaltyProgram);
        return loyaltyProgramMapper.toDto(updated);
    }
    public LoyaltyProgramResponseDto addSpentAmount(UUID userId, BigDecimal amount) {
        LoyaltyProgram loyaltyProgram = loyaltyProgramRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Loyalty program not found for user: " + userId));

        BigDecimal newTotalSpent = loyaltyProgram.getTotalSpent().add(amount);
        loyaltyProgram.setTotalSpent(newTotalSpent);

        int pointsToAdd = amount.divide(BigDecimal.valueOf(100)).intValue();
        if (pointsToAdd > 0) {
            loyaltyProgram.setTotalPoints(loyaltyProgram.getTotalPoints() + pointsToAdd);
            updateTierByPoints(loyaltyProgram);
        }

        LoyaltyProgram updated = loyaltyProgramRepository.save(loyaltyProgram);
        return loyaltyProgramMapper.toDto(updated);
    }
    private void updateTierByPoints(LoyaltyProgram loyaltyProgram) {
        int points = loyaltyProgram.getTotalPoints();

        if (points >= 10000) {
            loyaltyProgram.setTier(LoyaltyTier.PLATINUM);
        } else if (points >= 5000) {
            loyaltyProgram.setTier(LoyaltyTier.GOLD);
        } else if (points >= 1000) {
            loyaltyProgram.setTier(LoyaltyTier.SILVER);
        } else {
            loyaltyProgram.setTier(LoyaltyTier.BRONZE);
        }
    }
    public List<LoyaltyProgramResponseDto> getLoyaltyProgramsByTier(LoyaltyTier tier) {
        return loyaltyProgramRepository.findByTier(tier)
                .stream()
                .map(loyaltyProgramMapper::toDto)
                .collect(Collectors.toList());
    }
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
    public void deleteLoyaltyProgram(UUID loyaltyId) {
        LoyaltyProgram loyaltyProgram = loyaltyProgramRepository.findByLoyaltyId(loyaltyId)
                .orElseThrow(() -> new RuntimeException("Loyalty program not found with id: " + loyaltyId));

        loyaltyProgramRepository.delete(loyaltyProgram);
    }
}