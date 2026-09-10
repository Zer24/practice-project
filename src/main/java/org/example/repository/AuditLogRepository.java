package org.example.repository;

import org.example.domain.AuditLog;
import org.example.domain.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    List<AuditLog> findByPerformedBy(UUID performedBy);

    List<AuditLog> findByAction(AuditAction action);
    Page<AuditLog> findByAction(AuditAction action, Pageable pageable);

    Page<AuditLog> findByPerformedBy(UUID performedBy, Pageable pageable);
}