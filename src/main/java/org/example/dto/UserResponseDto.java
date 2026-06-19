// UserResponseDto.java
package org.example.dto;

import org.example.domain.enums.Role;
import org.example.domain.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponseDto(
        UUID userId,
        String username,
        String email,
        Role role,
        LocalDateTime createdAt
) {
    public UserResponseDto(User user) {
        this(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}