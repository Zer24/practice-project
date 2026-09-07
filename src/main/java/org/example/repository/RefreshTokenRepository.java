package org.example.repository;

import org.example.domain.RefreshToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUserId(UUID userId);

    void deleteByToken(String token);
    @Query("{ $and: [ " +
            "{ $or: [ { 'userId': ?0 }, { 'userId': { $exists': true } } ] }, " +
            "{ $or: [ { 'expiryDate': { $gte: ?1 } }, { 'expiryDate': { $exists': true } } ] }, " +
            "{ $or: [ { 'expiryDate': { $lte: ?2 } }, { 'expiryDate': { $exists': true } } ] } " +
            "] }")
    Page<RefreshToken> findRefreshTokensByFilters(UUID userId, LocalDateTime expiryFrom, LocalDateTime expiryTo, Pageable pageable);
}