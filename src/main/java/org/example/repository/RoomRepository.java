package org.example.repository;

import org.bson.types.ObjectId;
import org.example.domain.Room;
import org.example.domain.enums.RoomType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomRepository extends MongoRepository<Room, ObjectId>, RoomRepositoryCustom {

    Optional<Room> findByRoomId(UUID roomId);

    List<Room> findByRoomTypeAndIsDeletedFalse(RoomType roomType);

    @Query("{ 'hotelId': ?0, 'isDeleted': false }")
    List<Room> findActiveByHotelId(UUID hotelId);
}