package org.example.service;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.domain.enums.Role;
import org.example.domain.User;
import org.example.dto.UserCreateDto;
import org.example.dto.UserResponseDto;
import org.example.mapper.UserMapper;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Validated
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;


    @Transactional
    public UserResponseDto createUser(@Valid UserCreateDto dto) {
        if (userRepository.existsByUsernameAndIsDeletedFalse(dto.username())) {
            throw new RuntimeException("User with username " + dto.username() + " already exists");
        }
        if (userRepository.existsByEmailAndIsDeletedFalse(dto.email())) {
            throw new RuntimeException("User with email " + dto.email() + " already exists");
        }

        String hashedPassword = passwordEncoder.encode(dto.password());

        User user = userMapper.toEntity(dto);
        user.setPasswordHash(hashedPassword);
        if (user.getRole() == null) {
            user.setRole(Role.CUSTOMER);
        }

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Transactional
    public UserResponseDto updateUser(UUID userId, UserCreateDto request, UUID performedBy) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (user.isDeleted()) {
            throw new RuntimeException("Cannot update deleted user");
        }

        // Сохраняем старую роль для аудита
        Role oldRole = user.getRole();

        if (request.username() != null && !request.username().equals(user.getUsername())) {
            if (userRepository.existsByUsernameAndIsDeletedFalse(request.username())) {
                throw new RuntimeException("Username already taken");
            }
            user.setUsername(request.username());
        }

        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmailAndIsDeletedFalse(request.email())) {
                throw new RuntimeException("Email already taken");
            }
            user.setEmail(request.email());
        }

        if (request.password() != null) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        if (request.role() != null) {
            user.setRole(request.role());
        }

        User updatedUser = userRepository.save(user);

        // Логируем смену роли
        if (!oldRole.equals(user.getRole())) {
            auditService.logRoleChange(
                    userId,
                    oldRole.toString(),
                    user.getRole().toString(),
                    performedBy != null ? performedBy : userId
            );
        }

        return new UserResponseDto(updatedUser);
    }

    @Transactional
    public void softDeleteUser(UUID userId, UUID performedBy) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        user.setDeleted(true);
        userRepository.save(user);

        auditService.logUserDelete(userId, user.getUsername(), performedBy != null ? performedBy : userId, true);
    }

    @Transactional
    public void restoreUser(UUID userId, UUID performedBy) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        user.setDeleted(false);
        userRepository.save(user);

        auditService.logUserDelete(userId, user.getUsername(), performedBy != null ? performedBy : userId, false);
    }

    @Transactional
    public void hardDeleteUser(UUID userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        userRepository.delete(user);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsernameAndIsDeletedFalse(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmailAndIsDeletedFalse(email);
    }

    @Transactional
    public UserResponseDto authenticate(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isDeleted()) {
            throw new RuntimeException("Account is deleted");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        return new UserResponseDto(user);
    }

    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAllActive()
                .stream()
                .map(UserResponseDto::new)
                .collect(Collectors.toList());
    }

    public UserResponseDto getUserById(UUID userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return new UserResponseDto(user);
    }
}