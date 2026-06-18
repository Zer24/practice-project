package org.example.repository;

import org.example.domain.Hotel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HotelRepository extends MongoRepository<Hotel, String> {

    Optional<Hotel> findByHotelId(UUID hotelId);

    List<Hotel> findByIsDeletedFalse();

    List<Hotel> findByManagerIdAndIsDeletedFalse(UUID managerId);

    List<Hotel> findByCityAndIsDeletedFalse(String city);

    List<Hotel> findByCountryAndIsDeletedFalse(String country);

    boolean existsByHotelIdAndIsDeletedFalse(UUID hotelId);

    @Query("{ 'isDeleted': false }")
    List<Hotel> findAllActive();
}