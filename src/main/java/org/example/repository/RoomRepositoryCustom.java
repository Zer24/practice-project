// RoomRepositoryCustom.java
package org.example.repository;

import org.example.domain.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface RoomRepositoryCustom {

    Page<Room> findWithFilters(
            UUID hotelId,
            String roomType,
            Integer minCapacity,
            Integer maxCapacity,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String amenities,
            Pageable pageable
    );

    List<Room> findWithFilters(
            UUID hotelId,
            String roomType,
            Integer minCapacity,
            Integer maxCapacity,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String amenities
    );
}