package org.example.repository;

import org.example.domain.Booking;
import org.bson.types.ObjectId;
import org.example.domain.enums.BookingStatus;
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

    List<Booking> findByUserId(UUID userId);

    List<Booking> findByUserIdAndIsDeletedFalse(UUID userId);

    List<Booking> findByRoomId(UUID roomId);

    List<Booking> findByRoomIdAndIsDeletedFalse(UUID roomId);

    List<Booking> findByStatusAndIsDeletedFalse(BookingStatus status);

    List<Booking> findByUserIdAndStatusAndIsDeletedFalse(UUID userId, BookingStatus status);

    List<Booking> findByIsDeletedFalse();

    List<Booking> findByCheckInDateBetweenAndIsDeletedFalse(LocalDate startDate, LocalDate endDate);

    boolean existsByBookingIdAndIsDeletedFalse(UUID bookingId);

    @Query("{ 'isDeleted': false }")
    List<Booking> findAllActive();

    @Query("{ 'userId': ?0, 'isDeleted': false }")
    List<Booking> findActiveByUserId(UUID userId);

    @Query("{ 'roomId': ?0, 'checkInDate': { $lte: ?2 }, 'checkOutDate': { $gte: ?1 }, 'isDeleted': false }")
    List<Booking> findConflictingBookings(UUID roomId, LocalDate checkInDate, LocalDate checkOutDate);
}