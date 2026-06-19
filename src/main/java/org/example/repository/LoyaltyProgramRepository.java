package org.example.repository;

import org.example.domain.LoyaltyProgram;
import org.bson.types.ObjectId;
import org.example.domain.enums.LoyaltyTier;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoyaltyProgramRepository extends MongoRepository<LoyaltyProgram, ObjectId> {

    Optional<LoyaltyProgram> findByLoyaltyId(UUID loyaltyId);

    Optional<LoyaltyProgram> findByUserId(UUID userId);

    List<LoyaltyProgram> findByTier(LoyaltyTier tier);

    List<LoyaltyProgram> findByTotalPointsGreaterThanEqual(Integer points);

    boolean existsByUserId(UUID userId);


    List<LoyaltyProgram> findAll();
}