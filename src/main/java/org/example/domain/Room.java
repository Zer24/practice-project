package org.example.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.example.domain.enums.RoomType;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Document(collection = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @BsonId
    private String id;

    @BsonProperty("roomId")
    private UUID roomId = UUID.randomUUID();

    @BsonProperty("hotelId")
    private UUID hotelId;

    @BsonProperty("roomType")
    private RoomType roomType;

    @BsonProperty("pricePerNight")
    private BigDecimal pricePerNight;

    @BsonProperty("capacity")
    private int capacity;

    @BsonProperty("description")
    private String description;

    @BsonProperty("amenities")
    private List<String> amenities;

    @BsonProperty("area")
    private double area;

    @BsonProperty("isDeleted")
    private boolean isDeleted;
}