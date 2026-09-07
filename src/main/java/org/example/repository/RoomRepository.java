// RoomRepository.java
package org.example.repository;

import org.bson.types.ObjectId;
import org.example.domain.Room;
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
public interface RoomRepository extends MongoRepository<Room, ObjectId>, RoomRepositoryCustom {

    Optional<Room> findByRoomId(UUID roomId);

    // Базовые методы
    Page<Room> findByHotelIdAndIsDeletedFalse(UUID hotelId, Pageable pageable);
    List<Room> findByHotelIdAndIsDeletedFalse(UUID hotelId);

    List<Room> findByHotelId(UUID hotelId);
    Page<Room> findByHotelId(UUID hotelId, Pageable pageable);

    List<Room> findByIsDeletedFalse();

    List<Room> findByRoomTypeAndIsDeletedFalse(RoomType roomType);

    List<Room> findByCapacityGreaterThanEqualAndIsDeletedFalse(int capacity);

    boolean existsByRoomIdAndIsDeletedFalse(UUID roomId);

    @Query("{ 'isDeleted': false }")
    List<Room> findAllActive();

    @Query("{ 'hotelId': ?0, 'isDeleted': false }")
    List<Room> findActiveByHotelId(UUID hotelId);

    // Кастомный метод для поиска доступных комнат с фильтрацией по датам
    @Query(value = "{ 'hotelId': ?0, 'isDeleted': false }")
    List<Room> findAvailableRoomsByHotelId(UUID hotelId);
}