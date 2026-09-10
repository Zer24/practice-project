// RoomRepositoryCustomImpl.java
package org.example.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.Room;
import org.example.domain.enums.RoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RoomRepositoryCustomImpl implements RoomRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Room> findWithFilters(
            UUID hotelId,
            String roomType,
            Integer minCapacity,
            Integer maxCapacity,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String amenities,
            Pageable pageable) {

        Query query = buildDynamicQuery(hotelId, roomType, minCapacity, maxCapacity,
                minPrice, maxPrice, amenities);

        query.with(pageable);

        List<Room> rooms = mongoTemplate.find(query, Room.class);

        long total = mongoTemplate.count(query, Room.class);

        log.debug("Found {} rooms with filters: hotelId={}, roomType={}, minCapacity={}, maxCapacity={}, " +
                        "minPrice={}, maxPrice={}, amenities={}",
                total, hotelId, roomType, minCapacity, maxCapacity, minPrice, maxPrice, amenities);

        return new PageImpl<>(rooms, pageable, total);
    }

    @Override
    public List<Room> findWithFilters(
            UUID hotelId,
            String roomType,
            Integer minCapacity,
            Integer maxCapacity,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String amenities) {

        Query query = buildDynamicQuery(hotelId, roomType, minCapacity, maxCapacity,
                minPrice, maxPrice, amenities);
        return mongoTemplate.find(query, Room.class);
    }

    private Query buildDynamicQuery(
            UUID hotelId,
            String roomType,
            Integer minCapacity,
            Integer maxCapacity,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String amenities) {

        Query query = new Query();
        List<Criteria> andCriteria = new ArrayList<>();

        andCriteria.add(Criteria.where("isDeleted").is(false));

        if (hotelId != null) {
            andCriteria.add(Criteria.where("hotelId").is(hotelId));
        }

        if (StringUtils.hasText(roomType)) {
            try {
                roomType = URLDecoder.decode(roomType, StandardCharsets.UTF_8);
                RoomType type = RoomType.valueOf(roomType.toUpperCase());
                andCriteria.add(Criteria.where("roomType").is(type));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid room type: {}", roomType);
            }
        }

        if (minCapacity != null) {
            andCriteria.add(Criteria.where("capacity").gte(minCapacity));
        }
        if (maxCapacity != null) {
            andCriteria.add(Criteria.where("capacity").lte(maxCapacity));
        }

        if (minPrice != null) {
            andCriteria.add(Criteria.where("pricePerNight").gte(minPrice));
        }
        if (maxPrice != null) {
            andCriteria.add(Criteria.where("pricePerNight").lte(maxPrice));
        }

        if (StringUtils.hasText(amenities)) {
            amenities = URLDecoder.decode(amenities, StandardCharsets.UTF_8);
            andCriteria.add(Criteria.where("amenities")
                    .regex(amenities.trim(), "i"));
        }

        if (!andCriteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(
                    andCriteria.toArray(new Criteria[0])
            ));
        }

        log.debug("Built MongoDB query: {}", query);
        return query;
    }
}