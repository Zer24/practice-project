package org.example.dto;

public record AuthResponseDto(
        String token,
        String refreshToken,
        UserResponseDto user
) {}