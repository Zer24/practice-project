package org.example.repository;

import org.example.domain.CancelRequest;
import org.example.domain.enums.CancelRequestStatus;
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

    List<CancelRequest> findByBookingId(UUID bookingId);

    @Query("{ 'status': ?0 }")
    List<CancelRequest> findPendingRequests(CancelRequestStatus status);

    boolean existsByBookingIdAndStatus(UUID bookingId, CancelRequestStatus status);
}