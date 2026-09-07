package org.example.repository;

import org.example.domain.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HotelRepository extends MongoRepository<Hotel, String>, HotelRepositoryCustom {

    // Базовые методы для работы с отелями

    Optional<Hotel> findByHotelId(UUID hotelId);

    @Query("{ 'isDeleted': false }")
    List<Hotel> findByIsDeletedFalse();

    @Query("{ 'isDeleted': false }")
    Page<Hotel> findByIsDeletedFalse(Pageable pageable);

    @Query("{ 'city': ?0, 'isDeleted': false }")
    List<Hotel> findByCityAndIsDeletedFalse(String city);

    @Query("{ 'city': ?0, 'isDeleted': false }")
    Page<Hotel> findByCityAndIsDeletedFalse(String city, Pageable pageable);

    @Query("{ 'country': ?0, 'isDeleted': false }")
    List<Hotel> findByCountryAndIsDeletedFalse(String country);

    @Query("{ 'country': ?0, 'isDeleted': false }")
    Page<Hotel> findByCountryAndIsDeletedFalse(String country, Pageable pageable);

    @Query("{ 'managerId': ?0, 'isDeleted': false }")
    List<Hotel> findByManagerIdAndIsDeletedFalse(UUID managerId);

    @Query("{ 'managerId': ?0, 'isDeleted': false }")
    Page<Hotel> findByManagerIdAndIsDeletedFalse(UUID managerId, Pageable pageable);

    boolean existsByHotelIdAndIsDeletedFalse(UUID hotelId);
}