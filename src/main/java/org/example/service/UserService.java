package org.example.service;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.domain.User;
import org.example.domain.enums.Role;
import org.example.dto.UserCreateDto;
import org.example.dto.UserResponseDto;
import org.example.dto.UserUpdateDto;
import org.example.exception.DuplicateException;
import org.example.exception.InvalidPasswordException;
import org.example.mapper.UserMapper;
import org.example.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;
import java.util.UUID;

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
            throw new DuplicateException("User with username " + dto.username() + " already exists");
        }
        if (userRepository.existsByEmailAndIsDeletedFalse(dto.email())) {
            throw new DuplicateException("User with email " + dto.email() + " already exists");
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
    public UserResponseDto updateUser(UUID userId, UserUpdateDto request, UUID performedBy) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (user.isDeleted()) {
            throw new RuntimeException("Cannot update deleted user");
        }

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
    public UserResponseDto authenticate(String username, String password) {
        User user;
        Optional<User> usernameUser = userRepository.findByUsername(username);
        if(usernameUser.isPresent()){
            user = usernameUser.get();
        }else{
            Optional<User> emailUser = userRepository.findByEmail(username);
            if(emailUser.isPresent()){
                user = emailUser.get();
            }else{
                throw new RuntimeException("Пользователь не найден!");
            }
        }

        if (user.isDeleted()) {
            throw new RuntimeException("Account is deleted");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidPasswordException("Invalid password");
        }

        return new UserResponseDto(user);
    }

    public Page<UserResponseDto> getAllUsers(String role, String username, String email, Pageable pageable) {
        return userRepository.findActiveUsersByFilters(role, username, email, pageable)
                .map(UserResponseDto::new);
    }

    public UserResponseDto getUserById(UUID userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return new UserResponseDto(user);
    }
}