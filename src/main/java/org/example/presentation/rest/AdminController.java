package org.example.presentation.rest;

import lombok.RequiredArgsConstructor;
import org.example.domain.enums.Role;
import org.example.dto.UserResponseDto;
import org.example.dto.UserUpdateDto;
import org.example.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final SecurityUtils securityUtils;

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @PageableDefault() Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(role, username, email, pageable));
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<UserResponseDto> changeUserRole(
            @PathVariable UUID userId,
            @RequestParam String newRole) {
        UUID currentUserId = securityUtils.getCurrentUserId();
        try {
            Role role = Role.valueOf(newRole.toUpperCase());
            return ResponseEntity.ok(userService.updateUser(userId,
                    new UserUpdateDto(null, null, null, role), currentUserId));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + newRole);
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> softDeleteUser(@PathVariable UUID userId) {
        userService.softDeleteUser(userId, securityUtils.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{userId}/restore")
    public ResponseEntity<Void> restoreUser(@PathVariable UUID userId) {
        userService.restoreUser(userId, securityUtils.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

//    @DeleteMapping("/users/{userId}")
//    public ResponseEntity<Void> hardDeleteUser(@PathVariable UUID userId) {
//        userService.hardDeleteUser(userId);
//        return ResponseEntity.noContent().build();
//    }
}