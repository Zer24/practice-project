package org.example.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.example.domain.enums.BookingStatus;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Document(collection = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @BsonId
    private String id;

    @BsonProperty("bookingId")
    private UUID bookingId;

    @BsonProperty("userId")
    private UUID userId;

    @BsonProperty("roomId")
    private UUID roomId;

    @BsonProperty("checkInDate")
    private LocalDate checkInDate;

    @BsonProperty("checkOutDate")
    private LocalDate checkOutDate;

    @BsonProperty("totalPrice")
    private BigDecimal totalPrice;

    @BsonProperty("status")
    private BookingStatus status;

    @BsonProperty("isDeleted")
    private boolean isDeleted;

    public Booking(UUID userId, UUID roomId, LocalDate checkInDate,
                   LocalDate checkOutDate, BigDecimal totalPrice, BookingStatus status) {
        this.bookingId = UUID.randomUUID();
        this.userId = userId;
        this.roomId = roomId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalPrice = totalPrice;
        this.status = status;
        this.isDeleted = false;
    }
}