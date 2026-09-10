package org.example.dto;

public record AuthRequestDto(
        String login,
        String password
) {}