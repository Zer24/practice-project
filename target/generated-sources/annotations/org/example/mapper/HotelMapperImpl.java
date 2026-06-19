package org.example.mapper;

import java.util.UUID;
import javax.annotation.processing.Generated;
import org.example.domain.Hotel;
import org.example.dto.HotelCreateDto;
import org.example.dto.HotelResponseDto;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-19T23:11:03+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class HotelMapperImpl implements HotelMapper {

    @Override
    public HotelResponseDto toDto(Hotel hotel) {
        if ( hotel == null ) {
            return null;
        }

        UUID hotelId = null;
        String name = null;
        String city = null;
        String country = null;
        String address = null;
        Double rating = null;
        UUID managerId = null;

        hotelId = hotel.getHotelId();
        name = hotel.getName();
        city = hotel.getCity();
        country = hotel.getCountry();
        address = hotel.getAddress();
        rating = hotel.getRating();
        managerId = hotel.getManagerId();

        HotelResponseDto hotelResponseDto = new HotelResponseDto( hotelId, name, city, country, address, rating, managerId );

        return hotelResponseDto;
    }

    @Override
    public Hotel toEntity(HotelCreateDto dto) {
        if ( dto == null ) {
            return null;
        }

        Hotel hotel = new Hotel();

        hotel.setName( dto.name() );
        hotel.setCity( dto.city() );
        hotel.setCountry( dto.country() );
        hotel.setAddress( dto.address() );
        hotel.setManagerId( dto.managerId() );

        generateHotelId( hotel );

        return hotel;
    }
}
