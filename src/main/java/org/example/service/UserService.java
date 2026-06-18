package org.example.service;

import jakarta.validation.Valid;
import org.example.domain.Role;
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
@Transactional
public class UserService {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final UserMapper userMapper;
    @Autowired
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(UserRepository userRepository,
                       UserMapper userMapper,
                       PasswordEncoder passwordEncoder,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    public UserResponseDto createUser(@Valid UserCreateDto dto) {
        if (userRepository.existsByUsernameAndIsDeletedFalse(dto.getUsername())) {
            throw new RuntimeException("User with username " + dto.getUsername() + " already exists");
        }
        if (userRepository.existsByEmailAndIsDeletedFalse(dto.getEmail())) {
            throw new RuntimeException("User with email " + dto.getEmail() + " already exists");
        }

        String hashedPassword = passwordEncoder.encode(dto.getPassword());

        User user = userMapper.toEntity(dto);
        user.setPasswordHash(hashedPassword);
        if (user.getRole() == null) {
            user.setRole(Role.CUSTOMER);
        }

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    public UserResponseDto updateUser(UUID userId, UserCreateDto request, UUID performedBy) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (user.isDeleted()) {
            throw new RuntimeException("Cannot update deleted user");
        }

        // Сохраняем старую роль для аудита
        Role oldRole = user.getRole();

        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsernameAndIsDeletedFalse(request.getUsername())) {
                throw new RuntimeException("Username already taken");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
                throw new RuntimeException("Email already taken");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRole() != null) {
            user.setRole(request.getRole());
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

    public void softDeleteUser(UUID userId, UUID performedBy) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        user.setDeleted(true);
        userRepository.save(user);

        auditService.logUserDelete(userId, user.getUsername(), performedBy != null ? performedBy : userId, true);
    }

    public void restoreUser(UUID userId, UUID performedBy) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        user.setDeleted(false);
        userRepository.save(user);

        auditService.logUserDelete(userId, user.getUsername(), performedBy != null ? performedBy : userId, false);
    }

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