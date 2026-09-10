// HotelRepositoryCustom.java
package org.example.repository;

import org.example.domain.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HotelRepositoryCustom {

    Page<Hotel> findWithFilters(
            String city,
            String country,
            Double minRating,
            Double maxRating,
            Pageable pageable
    );

    List<Hotel> findWithFilters(
            String city,
            String country,
            Double minRating,
            Double maxRating
    );
}