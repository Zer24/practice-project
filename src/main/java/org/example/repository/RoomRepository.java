package org.example.repository;

import org.example.domain.Room;
import org.bson.types.ObjectId;
import org.example.domain.enums.RoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomRepository extends MongoRepository<Room, ObjectId> {

    Optional<Room> findByRoomId(UUID roomId);
    Page<Room> findByHotelIdAndIsDeletedFalse(UUID hotelId, Pageable pageable);

    List<Room> findByHotelId(UUID hotelId);

    List<Room> findByHotelIdAndIsDeletedFalse(UUID hotelId);

    List<Room> findByIsDeletedFalse();

    List<Room> findByRoomTypeAndIsDeletedFalse(RoomType roomType);

    List<Room> findByCapacityGreaterThanEqualAndIsDeletedFalse(int capacity);

    boolean existsByRoomIdAndIsDeletedFalse(UUID roomId);

    @Query("{ 'isDeleted': false }")
    List<Room> findAllActive();

    @Query("{ 'hotelId': ?0, 'isDeleted': false }")
    List<Room> findActiveByHotelId(UUID hotelId);
}