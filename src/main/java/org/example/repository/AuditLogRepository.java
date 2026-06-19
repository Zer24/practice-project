package org.example.repository;

import org.example.domain.enums.AuditAction;
import org.example.domain.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    List<AuditLog> findByPerformedBy(UUID performedBy);

    List<AuditLog> findByAction(AuditAction action);

    List<AuditLog> findByEntityTypeAndEntityId(String entityType, String entityId);

    @Query("{ 'performedAt': { $gte: ?0, $lte: ?1 } }")
    List<AuditLog> findByDateRange(LocalDateTime start, LocalDateTime end);

    List<AuditLog> findByActionAndEntityType(AuditAction action, String entityType);
}