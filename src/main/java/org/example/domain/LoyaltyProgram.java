package org.example.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.UUID;

@Document(collection = "loyalty_programs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyProgram {

    @BsonId
    private String id;

    @BsonProperty("loyaltyId")
    private UUID loyaltyId;

    @BsonProperty("userId")
    private UUID userId;

    @BsonProperty("totalPoints")
    private Integer totalPoints;

    @BsonProperty("tier")
    private LoyaltyTier tier;

    @BsonProperty("totalSpent")
    private BigDecimal totalSpent;

    public LoyaltyProgram(UUID userId, Integer totalPoints, LoyaltyTier tier, BigDecimal totalSpent) {
        this.loyaltyId = UUID.randomUUID();
        this.userId = userId;
        this.totalPoints = totalPoints;
        this.tier = tier;
        this.totalSpent = totalSpent;
    }
}