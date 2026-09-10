package org.example.repository;

import org.example.domain.CancelRequest;
import org.example.domain.enums.CancelRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CancelRequestRepository extends MongoRepository<CancelRequest, String> {

    Optional<CancelRequest> findByRequestId(UUID requestId);

    Page<CancelRequest> findByStatus(CancelRequestStatus status, Pageable pageable);
    Page<CancelRequest> findByUserId(UUID userId, Pageable pageable);

    boolean existsByBookingIdAndStatus(UUID bookingId, CancelRequestStatus status);
}