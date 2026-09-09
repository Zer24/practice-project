package org.example.repository;

import org.example.domain.Hotel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HotelRepository extends MongoRepository<Hotel, String>, HotelRepositoryCustom {
    Optional<Hotel> findByHotelId(UUID hotelId);

    boolean existsByNameAndCityAndIsDeletedFalse(String name, String city);
}