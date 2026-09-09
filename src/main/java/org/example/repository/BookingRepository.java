package org.example.repository;

import org.bson.types.ObjectId;
import org.example.domain.Booking;
import org.example.domain.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends MongoRepository<Booking, ObjectId> {

    Optional<Booking> findByBookingId(UUID bookingId);

    @Query("{ 'roomId': ?0, 'checkInDate': { $lte: ?2 }, 'checkOutDate': { $gte: ?1 }, 'isDeleted': false }")
    List<Booking> findConflictingBookings(UUID roomId, LocalDate checkInDate, LocalDate checkOutDate);

    @Query("{ 'status': { $in: ?2 }, 'checkInDate': { $lt: ?1 }, 'checkOutDate': { $gt: ?0 } }")
    List<Booking> findActiveBookingsByDateRange(
            LocalDate checkIn,
            LocalDate checkOut,
            List<BookingStatus> statuses
    );

    @Query("{ 'userId': ?0, 'isDeleted': false }")
    Page<Booking> findActiveBookingsByUserId(UUID userId, Pageable pageable);

    @Query("{ 'userId': ?0, 'status': ?1, 'isDeleted': false }")
    Page<Booking> findActiveBookingsByUserIdAndStatus(UUID userId, String status, Pageable pageable);

    @Query("{ 'userId': ?0, 'status': ?1, 'checkInDate': { $gte: ?2 }, 'checkOutDate': { $lte: ?3 }, 'isDeleted': false }")
    Page<Booking> findActiveBookingsByFiltersWithDates(
            UUID userId,
            String status,
            LocalDate checkInFrom,
            LocalDate checkOutTo,
            Pageable pageable
    );
}