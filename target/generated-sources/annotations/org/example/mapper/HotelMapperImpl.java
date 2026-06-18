package org.example.mapper;

import javax.annotation.processing.Generated;
import org.example.domain.Hotel;
import org.example.dto.HotelCreateDto;
import org.example.dto.HotelResponseDto;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-18T14:23:16+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class HotelMapperImpl implements HotelMapper {

    @Override
    public HotelResponseDto toDto(Hotel hotel) {
        if ( hotel == null ) {
            return null;
        }

        HotelResponseDto hotelResponseDto = new HotelResponseDto();

        hotelResponseDto.setHotelId( hotel.getHotelId() );
        hotelResponseDto.setName( hotel.getName() );
        hotelResponseDto.setCity( hotel.getCity() );
        hotelResponseDto.setCountry( hotel.getCountry() );
        hotelResponseDto.setAddress( hotel.getAddress() );
        hotelResponseDto.setRating( hotel.getRating() );
        hotelResponseDto.setManagerId( hotel.getManagerId() );

        return hotelResponseDto;
    }

    @Override
    public Hotel toEntity(HotelCreateDto dto) {
        if ( dto == null ) {
            return null;
        }

        Hotel hotel = new Hotel();

        hotel.setName( dto.getName() );
        hotel.setCity( dto.getCity() );
        hotel.setCountry( dto.getCountry() );
        hotel.setAddress( dto.getAddress() );
        hotel.setManagerId( dto.getManagerId() );

        generateHotelId( hotel );

        return hotel;
    }
}
