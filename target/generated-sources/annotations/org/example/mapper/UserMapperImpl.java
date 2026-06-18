package org.example.mapper;

import javax.annotation.processing.Generated;
import org.example.domain.User;
import org.example.dto.UserCreateDto;
import org.example.dto.UserResponseDto;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-18T13:51:36+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponseDto toDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponseDto userResponseDto = new UserResponseDto();

        userResponseDto.setUserId( user.getUserId() );
        userResponseDto.setUsername( user.getUsername() );
        userResponseDto.setEmail( user.getEmail() );
        userResponseDto.setRole( user.getRole() );
        userResponseDto.setCreatedAt( user.getCreatedAt() );

        return userResponseDto;
    }

    @Override
    public User toEntity(UserCreateDto dto) {
        if ( dto == null ) {
            return null;
        }

        User user = new User();

        user.setUsername( dto.getUsername() );
        user.setEmail( dto.getEmail() );
        user.setRole( dto.getRole() );

        generateUserId( user );

        return user;
    }
}
