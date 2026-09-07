package org.example.repository;

import org.example.domain.enums.AuditAction;
import org.example.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    Page<AuditLog> findByAction(AuditAction action, Pageable pageable);

    List<AuditLog> findByEntityTypeAndEntityId(String entityType, String entityId);

    @Query("{ 'performedAt': { $gte: ?0, $lte: ?1 } }")
    List<AuditLog> findByDateRange(LocalDateTime start, LocalDateTime end);

    List<AuditLog> findByActionAndEntityType(AuditAction action, String entityType);
    Page<AuditLog> findByPerformedBy(UUID performedBy, Pageable pageable);
    @Query("{ 'performedAt': { $gte: ?0, $lte: ?1 }, 'action': { $eq: ?2 } }")
    Page<AuditLog> findAuditLogsByDateRangeAndAction(LocalDateTime start, LocalDateTime end, String action, Pageable pageable);
    @Query("{ $and: [ " +
            "{ $or: [ { 'action': ?0 }, { 'action': { $exists': true } } ] }, " +
            "{ $or: [ { 'entityType': ?1 }, { 'entityType': { $exists': true } } ] }, " +
            "{ $or: [ { 'performedBy': ?2 }, { 'performedBy': { $exists': true } } ] }, " +
            "{ $or: [ { 'performedAt': { $gte: ?3 } }, { 'performedAt': { $exists': true } } ] }, " +
            "{ $or: [ { 'performedAt': { $lte: ?4 } }, { 'performedAt': { $exists': true } } ] } " +
            "] }")
    Page<AuditLog> findAuditLogsByFilters(String action, String entityType, UUID performedBy, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}