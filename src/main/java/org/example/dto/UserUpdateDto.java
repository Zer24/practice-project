package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.example.domain.enums.Role;

@Schema(description = "DTO для создания пользователя")
public record UserUpdateDto(
        @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
        @Schema(description = "Имя пользователя", example = "hanna")
        String username,

        @Email
        @Schema(description = "Почта", example = "example@example.com")
        String email,

        @Size(min = 4, message = "Password min 4 chars")
        @Schema(description = "Пароль", example = "1234")
        String password,

        @Schema(description = "Роль пользователя", example = "CUSTOMER")
        Role role
) {}