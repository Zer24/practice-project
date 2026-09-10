package org.example.presentation.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.*;
import org.example.security.JwtTokenProvider;
import org.example.service.RefreshTokenService;
import org.example.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API для аутентификации и управления токенами")
@Validated
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserCreateDto dto) {
        UserResponseDto user = userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @Operation(
            summary = "Вход в систему",
            description = "Аутентифицирует пользователя и возвращает JWT токены доступа и обновления"
    )
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody AuthRequestDto request) {
        UserResponseDto user = userService.authenticate(request.login(), request.password()); // удалённый пользователь не может залогиниться по сервису

        String accessToken = tokenProvider.generateAccessToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user);

        refreshTokenService.saveRefreshToken(user.userId(), refreshToken);

        return ResponseEntity.ok(new AuthResponseDto(accessToken, refreshToken, user));
    }

    @Operation(
            summary = "Обновление токена доступа",
            description = "Использует refresh токен для получения новой пары токенов"
    )
    @PostMapping("/tokens")
    public ResponseEntity<AuthResponseDto> refresh(@Valid @RequestBody RefreshTokenRequestDto request) {
        String refreshToken = request.refreshToken();

        if (!tokenProvider.validateToken(refreshToken))
            throw new RuntimeException("Invalid refresh token");

        if (!"refresh".equals(tokenProvider.getTokenType(refreshToken)))
            throw new RuntimeException("Invalid token type");

        refreshTokenService.validateRefreshToken(refreshToken);

        UUID userId = tokenProvider.getUserIdFromToken(refreshToken);
        UserResponseDto user = userService.getUserById(userId);

        String newAccessToken = tokenProvider.generateAccessToken(user);
        String newRefreshToken = tokenProvider.generateRefreshToken(user);

        refreshTokenService.revokeRefreshToken(refreshToken);

        refreshTokenService.saveRefreshToken(user.userId(), newRefreshToken);

        return ResponseEntity.ok(new AuthResponseDto(newAccessToken, newRefreshToken, user));
    }

    @Operation(
            summary = "Выход из системы",
            description = "Отзывает refresh токен, делая его недействительным"
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization") String authorization) {
        String token = authorization.substring(7);

        if (tokenProvider.validateToken(token) && "refresh".equals(tokenProvider.getTokenType(token))) {
            refreshTokenService.revokeRefreshToken(token);
        }

        return ResponseEntity.noContent().build();
    }
}