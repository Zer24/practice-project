package org.example.mapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.example.domain.Room;
import org.example.domain.enums.RoomType;
import org.example.dto.RoomCreateDto;
import org.example.dto.RoomResponseDto;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-19T23:11:03+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class RoomMapperImpl implements RoomMapper {

    @Override
    public RoomResponseDto toDto(Room room) {
        if ( room == null ) {
            return null;
        }

        UUID roomId = null;
        UUID hotelId = null;
        RoomType roomType = null;
        BigDecimal pricePerNight = null;
        Integer capacity = null;
        String description = null;
        List<String> amenities = null;
        Double area = null;

        roomId = room.getRoomId();
        hotelId = room.getHotelId();
        roomType = room.getRoomType();
        pricePerNight = room.getPricePerNight();
        capacity = room.getCapacity();
        description = room.getDescription();
        List<String> list = room.getAmenities();
        if ( list != null ) {
            amenities = new ArrayList<String>( list );
        }
        area = room.getArea();

        RoomResponseDto roomResponseDto = new RoomResponseDto( roomId, hotelId, roomType, pricePerNight, capacity, description, amenities, area );

        return roomResponseDto;
    }

    @Override
    public Room toEntity(RoomCreateDto dto) {
        if ( dto == null ) {
            return null;
        }

        Room room = new Room();

        room.setHotelId( dto.hotelId() );
        room.setRoomType( dto.roomType() );
        room.setPricePerNight( dto.pricePerNight() );
        if ( dto.capacity() != null ) {
            room.setCapacity( dto.capacity() );
        }
        room.setDescription( dto.description() );
        List<String> list = dto.amenities();
        if ( list != null ) {
            room.setAmenities( new ArrayList<String>( list ) );
        }
        if ( dto.area() != null ) {
            room.setArea( dto.area() );
        }

        generateUserId( room );

        return room;
    }
}
