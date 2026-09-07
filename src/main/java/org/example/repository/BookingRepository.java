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
    Page<Booking> findByUserIdAndIsDeletedFalse(UUID userId, Pageable pageable);
    Page<Booking> findByRoomIdInAndIsDeletedFalse(List<UUID> roomIds, Pageable pageable);
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
//    @Query("{ 'isDeleted': false, 'userId': { $eq: ?0 }, 'status': { $eq: ?1 }, 'checkInDate': { $gte: ?2 }, 'checkOutDate': { $lte: ?3 } }")
//    Page<Booking> findActiveBookingsByFilters(UUID userId, String status, LocalDate checkInFrom, LocalDate checkOutTo, Pageable pageable);

    @Query("{ 'isDeleted': false, 'roomId': { $eq: ?0 }, 'status': { $eq: ?1 } }")
    Page<Booking> findActiveBookingsByRoomAndStatus(UUID roomId, String status, Pageable pageable);
    // BookingRepository.java (добавить метод)
    @Query("{ 'roomId': { $in: ?0 }, 'status': { $in: ?1 }, 'checkInDate': { $lt: ?2 }, 'checkOutDate': { $gt: ?3 } }")
    List<Booking> findBookingsByRoomIdsAndStatusAndDateRange(
            List<UUID> roomIds,
            List<BookingStatus> statuses,
            LocalDate checkOut,
            LocalDate checkIn
    );

    // Или более простой вариант:
    @Query("{ 'status': { $in: ?2 }, 'checkInDate': { $lt: ?1 }, 'checkOutDate': { $gt: ?0 } }")
    List<Booking> findActiveBookingsByDateRange(
            LocalDate checkIn,
            LocalDate checkOut,
            List<BookingStatus> statuses
    );
    @Query("{ 'isDeleted': false, $and: [ " +
            "{ $or: [ { 'userId': ?0 }, { 'userId': { $exists': true } } ] }, " +
            "{ $or: [ { 'roomId': ?1 }, { 'roomId': { $exists': true } } ] }, " +
            "{ $or: [ { 'status': ?2 }, { 'status': { $exists': true } } ] }, " +
            "{ $or: [ { 'checkInDate': { $gte: ?3 } }, { 'checkInDate': { $exists': true } } ] }, " +
            "{ $or: [ { 'checkOutDate': { $lte: ?4 } }, { 'checkOutDate': { $exists': true } } ] } " +
            "] }")
    Page<Booking> findActiveBookingsByFilters(UUID userId, UUID roomId, String status, LocalDate checkInFrom, LocalDate checkOutTo, Pageable pageable);
}