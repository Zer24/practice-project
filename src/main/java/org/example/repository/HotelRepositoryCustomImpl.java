package org.example.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class HotelRepositoryCustomImpl implements HotelRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Hotel> findWithFilters(
            String city,
            String country,
            Double minRating,
            Double maxRating,
            Pageable pageable) {

        Query query = buildDynamicQuery(city, country, minRating, maxRating);

        query.with(pageable);

        List<Hotel> hotels = mongoTemplate.find(query, Hotel.class);

        Query countQuery = buildDynamicQuery(city, country, minRating, maxRating);
        long total = mongoTemplate.count(countQuery, Hotel.class);

        log.debug("Found {} hotels with filters: city={}, country={}, minRating={}, maxRating={}",
                total, city, country, minRating, maxRating);

        return new PageImpl<>(hotels, pageable, total);
    }

    @Override
    public List<Hotel> findWithFilters(
            String city,
            String country,
            Double minRating,
            Double maxRating) {

        Query query = buildDynamicQuery(city, country, minRating, maxRating);
        return mongoTemplate.find(query, Hotel.class);
    }
    private Query buildDynamicQuery(String city, String country, Double minRating, Double maxRating) {
        Query query = new Query();

        List<Criteria> andCriteria = new ArrayList<>();

        andCriteria.add(Criteria.where("isDeleted").is(false));

        if (StringUtils.hasText(city)) {
            city = URLDecoder.decode(city, StandardCharsets.UTF_8);
            andCriteria.add(Criteria.where("city").regex(city.trim(), "i"));
//            andCriteria.add(Criteria.where("city").regex(Pattern.quote(city.trim()), "i"));
//            andCriteria.add(Criteria.where("city")
//                    .regex("^" + city.trim() + "$", "i")); // точное совпадение без учета регистра
            // .regex(city.trim(), "i")
//            Pattern pattern = Pattern.compile(Pattern.quote(city.trim()), Pattern.CASE_INSENSITIVE);
//             andCriteria.add(Criteria.where("city").regex(pattern));
        }

        if (StringUtils.hasText(country)) {
            country = URLDecoder.decode(country, StandardCharsets.UTF_8);
            andCriteria.add(Criteria.where("country").regex("^" + country.trim() + "$", "i"));
//            andCriteria.add(Criteria.where("country").regex("^" + Pattern.quote(country.trim()) + "$", "i"));
//            andCriteria.add(Criteria.where("country")
//                    .regex("^" + country.trim() + "$", "i"));
//            Pattern pattern = Pattern.compile("^" + Pattern.quote(country.trim()) + "$", Pattern.CASE_INSENSITIVE);
//            andCriteria.add(Criteria.where("country").regex(pattern));
        }

        if (minRating != null && maxRating != null) {
            andCriteria.add(Criteria.where("rating")
                    .gte(minRating)
                    .lte(maxRating));
        } else if (minRating != null) {
            andCriteria.add(Criteria.where("rating").gte(minRating));
        } else if (maxRating != null) {
            andCriteria.add(Criteria.where("rating").lte(maxRating));
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