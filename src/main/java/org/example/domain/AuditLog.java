package org.example.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.example.domain.enums.AuditAction;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @BsonId
    private String id;

    @BsonProperty("auditId")
    private UUID auditId;

    @BsonProperty("action")
    private AuditAction action;

    @BsonProperty("entityType")
    private String entityType;

    @BsonProperty("entityId")
    private String entityId;

    @BsonProperty("performedBy")
    private UUID performedBy;

    @BsonProperty("performedAt")
    private LocalDateTime performedAt;

    @BsonProperty("oldValue")
    private String oldValue;

    @BsonProperty("newValue")
    private String newValue;

    @BsonProperty("details")
    private String details;

    public AuditLog(AuditAction action, String entityType, String entityId,
                    UUID performedBy, String oldValue, String newValue, String details) {
        this.auditId = UUID.randomUUID();
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.performedBy = performedBy;
        this.performedAt = LocalDateTime.now();
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.details = details;
    }
}