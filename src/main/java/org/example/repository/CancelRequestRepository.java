package org.example.repository;

import org.example.domain.CancelRequest;
import org.example.domain.enums.CancelRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CancelRequestRepository extends MongoRepository<CancelRequest, String> {

    Optional<CancelRequest> findByRequestId(UUID requestId);

    List<CancelRequest> findByUserId(UUID userId);

    List<CancelRequest> findByStatus(CancelRequestStatus status);
    Page<CancelRequest> findByStatus(CancelRequestStatus status, Pageable pageable);
    Page<CancelRequest> findByUserId(UUID userId, Pageable pageable);

    List<CancelRequest> findByBookingId(UUID bookingId);

    @Query("{ 'status': ?0 }")
    List<CancelRequest> findPendingRequests(CancelRequestStatus status);

    boolean existsByBookingIdAndStatus(UUID bookingId, CancelRequestStatus status);
    @Query("{ $and: [ " +
            "{ $or: [ { 'userId': ?0 }, { 'userId': { $exists': true } } ] }, " +
            "{ $or: [ { 'status': ?1 }, { 'status': { $exists': true } } ] }, " +
            "{ $or: [ { 'bookingId': ?2 }, { 'bookingId': { $exists': true } } ] } " +
            "] }")
    Page<CancelRequest> findCancelRequestsByFilters(UUID userId, String status, UUID bookingId, Pageable pageable);
}