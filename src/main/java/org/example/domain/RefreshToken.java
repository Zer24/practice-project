package org.example.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "refresh_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @BsonId
    private String id;

    @BsonProperty("tokenId")
    private UUID tokenId;

    @BsonProperty("userId")
    private UUID userId;

    @BsonProperty("token")
    private String token;

    @BsonProperty("expiryDate")
    private LocalDateTime expiryDate;

    @BsonProperty("revoked")
    private boolean revoked;

    public RefreshToken(UUID userId, String token, LocalDateTime expiryDate) {
        this.tokenId = UUID.randomUUID();
        this.userId = userId;
        this.token = token;
        this.expiryDate = expiryDate;
        this.revoked = false;
    }
}