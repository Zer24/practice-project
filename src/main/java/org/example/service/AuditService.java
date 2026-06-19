package org.example.service;

import org.example.domain.enums.AuditAction;
import org.example.domain.AuditLog;
import org.example.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void logRoleChange(UUID userId, String oldRole, String newRole, UUID performedBy) {
        AuditLog log = new AuditLog(
                AuditAction.ROLE_CHANGE,
                "User",
                userId.toString(),
                performedBy,
                oldRole,
                newRole,
                "Role changed from " + oldRole + " to " + newRole
        );
        auditLogRepository.save(log);
    }

    @Transactional
    public void logHotelDelete(UUID hotelId, String hotelName, UUID performedBy, boolean softDelete) {
        AuditAction action = softDelete ? AuditAction.HOTEL_DELETE : AuditAction.HOTEL_RESTORE;
        String details = softDelete ? "Hotel soft deleted: " + hotelName : "Hotel restored: " + hotelName;

        AuditLog log = new AuditLog(
                action,
                "Hotel",
                hotelId.toString(),
                performedBy,
                null,
                hotelName,
                details
        );
        auditLogRepository.save(log);
    }

    @Transactional
    public void logUserDelete(UUID userId, String username, UUID performedBy, boolean softDelete) {
        AuditAction action = softDelete ? AuditAction.USER_DELETE : AuditAction.USER_RESTORE;
        String details = softDelete ? "User soft deleted: " + username : "User restored: " + username;

        AuditLog log = new AuditLog(
                action,
                "User",
                userId.toString(),
                performedBy,
                null,
                username,
                details
        );
        auditLogRepository.save(log);
    }

    public List<AuditLog> getAuditLogsByAction(AuditAction action) {
        return auditLogRepository.findByAction(action);
    }

    public List<AuditLog> getAuditLogsByUser(UUID userId) {
        return auditLogRepository.findByPerformedBy(userId);
    }

    public List<AuditLog> getAuditLogsByEntity(String entityType, String entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByDateRange(start, end);
    }

    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }
}