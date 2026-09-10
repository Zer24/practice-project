package org.example.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "hotels")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hotel {

    @BsonId
    private String id;

    @BsonProperty("hotelId")
    private UUID hotelId;

    @BsonProperty("name")
    private String name;

    @BsonProperty("city")
    private String city;

    @BsonProperty("country")
    private String country;

    @BsonProperty("address")
    private String address;

    @BsonProperty("managerId")
    private UUID managerId;

    @BsonProperty("isDeleted")
    private boolean isDeleted;

    @BsonProperty("rating")
    private Double rating;

    public Hotel(String name, String city, String country, String address, UUID managerId, Double rating) {
        this.hotelId = UUID.randomUUID();
        this.name = name;
        this.city = city;
        this.country = country;
        this.address = address;
        this.managerId = managerId;
        this.isDeleted = false;
        this.rating=rating;
    }
}