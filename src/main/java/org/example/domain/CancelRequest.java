package org.example.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.example.domain.enums.CancelRequestStatus;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "cancel_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelRequest {

    @BsonId
    private String id;

    @BsonProperty("requestId")
    private UUID requestId;

    @BsonProperty("bookingId")
    private UUID bookingId;

    @BsonProperty("userId")
    private UUID userId;

    @BsonProperty("reason")
    private String reason;

    @BsonProperty("status")
    private CancelRequestStatus status;

    @BsonProperty("createdAt")
    private LocalDateTime createdAt;

    @BsonProperty("processedAt")
    private LocalDateTime processedAt;

    @BsonProperty("processedBy")
    private UUID processedBy;

    public CancelRequest(UUID bookingId, UUID userId, String reason) {
        this.requestId = UUID.randomUUID();
        this.bookingId = bookingId;
        this.userId = userId;
        this.reason = reason;
        this.status = CancelRequestStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }
}