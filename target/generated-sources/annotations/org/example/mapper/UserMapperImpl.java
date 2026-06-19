package org.example.mapper;

import java.time.LocalDateTime;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.example.domain.User;
import org.example.domain.enums.Role;
import org.example.dto.UserCreateDto;
import org.example.dto.UserResponseDto;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-19T23:11:03+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponseDto toDto(User user) {
        if ( user == null ) {
            return null;
        }

        UUID userId = null;
        String username = null;
        String email = null;
        Role role = null;
        LocalDateTime createdAt = null;

        userId = user.getUserId();
        username = user.getUsername();
        email = user.getEmail();
        role = user.getRole();
        createdAt = user.getCreatedAt();

        UserResponseDto userResponseDto = new UserResponseDto( userId, username, email, role, createdAt );

        return userResponseDto;
    }

    @Override
    public User toEntity(UserCreateDto dto) {
        if ( dto == null ) {
            return null;
        }

        User user = new User();

        user.setUsername( dto.username() );
        user.setEmail( dto.email() );
        user.setRole( dto.role() );

        generateUserId( user );

        return user;
    }
}
