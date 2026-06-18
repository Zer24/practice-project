package org.example.mapper;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.example.domain.Room;
import org.example.dto.RoomCreateDto;
import org.example.dto.RoomResponseDto;
import org.example.dto.RoomUpdateDto;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-18T13:51:36+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class RoomMapperImpl implements RoomMapper {

    @Override
    public RoomResponseDto toDto(Room room) {
        if ( room == null ) {
            return null;
        }

        RoomResponseDto roomResponseDto = new RoomResponseDto();

        roomResponseDto.setRoomId( room.getRoomId() );
        roomResponseDto.setHotelId( room.getHotelId() );
        roomResponseDto.setRoomType( room.getRoomType() );
        roomResponseDto.setPricePerNight( room.getPricePerNight() );
        roomResponseDto.setCapacity( room.getCapacity() );
        roomResponseDto.setDescription( room.getDescription() );
        List<String> list = room.getAmenities();
        if ( list != null ) {
            roomResponseDto.setAmenities( new ArrayList<String>( list ) );
        }
        roomResponseDto.setArea( room.getArea() );

        return roomResponseDto;
    }

    @Override
    public Room toEntity(RoomCreateDto dto) {
        if ( dto == null ) {
            return null;
        }

        Room room = new Room();

        room.setHotelId( dto.getHotelId() );
        room.setRoomType( dto.getRoomType() );
        room.setPricePerNight( dto.getPricePerNight() );
        if ( dto.getCapacity() != null ) {
            room.setCapacity( dto.getCapacity() );
        }
        room.setDescription( dto.getDescription() );
        List<String> list = dto.getAmenities();
        if ( list != null ) {
            room.setAmenities( new ArrayList<String>( list ) );
        }
        if ( dto.getArea() != null ) {
            room.setArea( dto.getArea() );
        }

        generateUserId( room );

        return room;
    }

    @Override
    public void updateEntity(Room room, RoomUpdateDto dto) {
        if ( dto == null ) {
            return;
        }

        room.setRoomType( dto.getRoomType() );
        room.setPricePerNight( dto.getPricePerNight() );
        if ( dto.getCapacity() != null ) {
            room.setCapacity( dto.getCapacity() );
        }
        room.setDescription( dto.getDescription() );
        if ( room.getAmenities() != null ) {
            List<String> list = dto.getAmenities();
            if ( list != null ) {
                room.getAmenities().clear();
                room.getAmenities().addAll( list );
            }
            else {
                room.setAmenities( null );
            }
        }
        else {
            List<String> list = dto.getAmenities();
            if ( list != null ) {
                room.setAmenities( new ArrayList<String>( list ) );
            }
        }
        if ( dto.getArea() != null ) {
            room.setArea( dto.getArea() );
        }

        generateUserId( room );
    }
}
