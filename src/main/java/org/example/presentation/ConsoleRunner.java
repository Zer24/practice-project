package org.example.presentation;

import org.example.domain.*;
import org.example.dto.*;
import org.example.service.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ConsoleRunner implements CommandLineRunner {

    private final UserService userService;
    private final HotelService hotelService;
    private final RoomService roomService;
    private final BookingService bookingService;
    private final LoyaltyProgramService loyaltyProgramService;
    private final CancelRequestService cancelRequestService;
    private final AuditService auditService;

    private UserResponseDto currentUser;
    private Scanner scanner;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public ConsoleRunner(UserService userService,
                         HotelService hotelService,
                         RoomService roomService,
                         BookingService bookingService,
                         LoyaltyProgramService loyaltyProgramService,
                         CancelRequestService cancelRequestService,
                         AuditService auditService) {
        this.userService = userService;
        this.hotelService = hotelService;
        this.roomService = roomService;
        this.bookingService = bookingService;
        this.loyaltyProgramService = loyaltyProgramService;
        this.cancelRequestService = cancelRequestService;
        this.auditService = auditService;
    }

    @Override
    public void run(String... args) {
        initAdminUser();
        scanner = new Scanner(System.in);
        System.out.println("═══════════════════════════════════════");
        System.out.println("     ДОБРО ПОЖАЛОВАТЬ В HOTEL BOOKING SYSTEM");
        System.out.println("═══════════════════════════════════════");

        System.out.println("[DEBUG] ad");

        while (currentUser == null) {
            try {
                showAuthMenu();
            } catch (RuntimeException e) {
                System.out.println("❌ Ошибка: " + e.getMessage());
            }
        }

        System.out.println("\n✅ Добро пожаловать, " + currentUser.getUsername() + "!");
        System.out.println("Ваша роль: " + currentUser.getRole());

        if (currentUser.getRole() == Role.CUSTOMER) {
            showLoyaltyInfo();
        }

        while (true) {
            try {
                showMainMenu();
                String command = scanner.nextLine().trim();
                if (command.equalsIgnoreCase("logout")) {
                    currentUser = null;
                    System.out.println("Вы вышли из системы.\n");
                    while (currentUser == null) {
                        showAuthMenu();
                    }
                    System.out.println("\n✅ С возвращением, " + currentUser.getUsername() + "!");
                    if (currentUser.getRole() == Role.CUSTOMER) {
                        showLoyaltyInfo();
                    }
                } else if (command.equalsIgnoreCase("exit")) {
                    System.out.println("До свидания!");
                    System.exit(0);
                } else {
                    processCommand(command);
                }
            } catch (Exception e) {
                System.out.println("❌ Ошибка: " + e.getMessage());
            }
        }
    }

    private void initAdminUser() {
        if (!userService.existsByUsername("admin")) {
            userService.createUser(new UserCreateDto("admin", "admin@admin.com", "1234", Role.ADMIN));
            System.out.println("✅ Создан администратор по умолчанию (admin/1234)");
        }
    }

    private void showAuthMenu() {
        System.out.println("\n---------------------------------------");
        System.out.println("1. Вход в систему");
        System.out.println("2. Регистрация нового пользователя");
        System.out.println("3. Выход");
        System.out.print("Выберите опцию: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> login();
            case "2" -> register();
            case "3" -> {
                System.out.println("До свидания!");
                System.exit(0);
            }
            default -> System.out.println("Неверный выбор. Попробуйте снова.");
        }
    }

    private void login() {
        System.out.print("Логин (email или username): ");
        String login = scanner.nextLine().trim();
        System.out.print("Пароль: ");
        String password = scanner.nextLine().trim();

        currentUser = userService.authenticate(login, password);
        System.out.println("✅ Вход выполнен успешно!");
    }

    private void register() {
        System.out.print("Имя пользователя: ");
        String username = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Пароль (мин. 4 символа): ");
        String password = scanner.nextLine().trim();

        if (password.length() < 4) {
            System.out.println("❌ Пароль должен быть не менее 4 символов");
            return;
        }

        try {
            UserCreateDto dto = new UserCreateDto(username, email, password, Role.CUSTOMER);
            userService.createUser(dto);
            System.out.println("✅ Регистрация успешна! Теперь вы можете войти.");
        } catch (RuntimeException e) {
            System.out.println("❌ Ошибка регистрации: " + e.getMessage());
        }
    }

    private void showLoyaltyInfo() {
        try {
            LoyaltyProgramResponseDto loyalty = loyaltyProgramService.getLoyaltyProgramByUser(currentUser.getUserId());
            System.out.println("\n⭐ ПРОГРАММА ЛОЯЛЬНОСТИ ⭐");
            System.out.println("   Уровень: " + loyalty.getTier());
            System.out.println("   Баллов: " + loyalty.getTotalPoints());
            System.out.println("   Всего потрачено: $" + loyalty.getTotalSpent());
            System.out.println("   Доступная скидка: " + getDiscountInfo(loyalty.getTier()));
        } catch (RuntimeException e) {
            // У пользователя еще нет программы лояльности
        }
    }

    private String getDiscountInfo(LoyaltyTier tier) {
        return switch (tier) {
            case BRONZE -> "5%";
            case SILVER -> "10% + бонус";
            case GOLD -> "15% + бонус";
            case PLATINUM -> "20% + бонус";
        };
    }

    private void showMainMenu() {
        System.out.println("\n═══════════════════════════════════════");
        System.out.println("ГЛАВНОЕ МЕНЮ [Роль: " + currentUser.getRole() + "]");
        System.out.println("═══════════════════════════════════════");

        switch (currentUser.getRole()) {
            case CUSTOMER -> showCustomerMenu();
            case MANAGER -> showManagerMenu();
            case ADMIN -> showAdminMenu();
        }
        System.out.println("\nlogout - выйти из аккаунта");
        System.out.println("exit - завершить работу");
        System.out.print("\n> ");
    }

    private void showCustomerMenu() {
        System.out.println("1. Просмотр доступных отелей");
        System.out.println("2. Поиск свободных комнат");
        System.out.println("3. Мои бронирования");
        System.out.println("4. Создать бронирование");
        System.out.println("5. Отменить бронирование");
        System.out.println("6. Моя программа лояльности");
        System.out.println("7. Запросить отмену бронирования");
        System.out.println("8. Статус моих запросов на отмену");
    }

    private void showManagerMenu() {
        System.out.println("1. Мои отели");
        System.out.println("2. Бронирования моего отеля");
        System.out.println("3. Создать бронирование для клиента");
        System.out.println("4. Просмотр комнат моего отеля");
        System.out.println("5. Подтвердить/отменить бронирование");
        System.out.println("6. Управление запросами на отмену");
        System.out.println("7. Просмотр аудита (мои действия)");
    }

    private void showAdminMenu() {
        System.out.println("--- Управление отелями ---");
        System.out.println("1. Все отели");
        System.out.println("2. Создать отель");
        System.out.println("3. Редактировать отель");
        System.out.println("4. Удалить/восстановить отель");
        System.out.println("--- Управление пользователями ---");
        System.out.println("5. Все пользователи");
        System.out.println("6. Создать пользователя");
        System.out.println("7. Редактировать пользователя");
        System.out.println("8. Удалить/восстановить пользователя");
        System.out.println("--- Управление номерами ---");
        System.out.println("9. Номера отеля");
        System.out.println("10. Создать номер");
        System.out.println("11. Редактировать номер");
        System.out.println("--- Бронирования ---");
        System.out.println("12. Бронирования отеля");
        System.out.println("13. Создать бронирование");
        System.out.println("14. Подтвердить/отменить бронирование");
        System.out.println("--- Запросы на отмену ---");
        System.out.println("15. Все запросы на отмену");
        System.out.println("16. Обработать запрос на отмену");
        System.out.println("--- Аудит ---");
        System.out.println("17. Просмотр всех аудит-логов");
        System.out.println("18. Аудит по действию");
    }

    private void processCommand(String command) {
        switch (currentUser.getRole()) {
            case CUSTOMER -> processCustomerCommand(command);
            case MANAGER -> processManagerCommand(command);
            case ADMIN -> processAdminCommand(command);
        }
    }

    private void processCustomerCommand(String command) {
        switch (command) {
            case "1" -> showAllHotels();
            case "2" -> searchAvailableRooms();
            case "3" -> showMyBookings();
            case "4" -> createBookingForCustomer();
            case "5" -> cancelMyBooking();
            case "6" -> showMyLoyalty();
            case "7" -> requestCancellation();
            case "8" -> showMyCancelRequests();
            default -> System.out.println("❌ Неизвестная команда");
        }
    }

    private void requestCancellation() {
        System.out.print("Введите ID бронирования для отмены: ");
        UUID bookingId;
        try {
            bookingId = UUID.fromString(scanner.nextLine().trim());
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Неверный формат ID");
            return;
        }

        System.out.print("Причина отмены: ");
        String reason = scanner.nextLine().trim();
        if (reason.isEmpty()) {
            System.out.println("❌ Причина отмены обязательна");
            return;
        }

        try {
            CancelRequestCreateDto dto = new CancelRequestCreateDto(bookingId, reason);
            CancelRequestDto request = cancelRequestService.createCancelRequest(dto, currentUser.getUserId());
            System.out.println("✅ Запрос на отмену создан! ID: " + request.getRequestId());
            System.out.println("   Статус: " + request.getStatus());
            System.out.println("   Ожидайте подтверждения от менеджера отеля.");
        } catch (RuntimeException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }
    private void showMyCancelRequests() {
        List<CancelRequestDto> requests = cancelRequestService.getCancelRequestsByUser(currentUser.getUserId());
        if (requests.isEmpty()) {
            System.out.println("У вас нет запросов на отмену.");
            return;
        }

        System.out.println("\n=== МОИ ЗАПРОСЫ НА ОТМЕНУ ===");
        for (CancelRequestDto request : requests) {
            System.out.printf("📋 Запрос #%s%n", request.getRequestId());
            System.out.printf("   Бронирование: %s%n", request.getBookingId());
            System.out.printf("   Причина: %s%n", request.getReason());
            System.out.printf("   Статус: %s%n", request.getStatus());
            System.out.printf("   Создан: %s%n", request.getCreatedAt());
            if (request.getProcessedAt() != null) {
                System.out.printf("   Обработан: %s%n", request.getProcessedAt());
                System.out.printf("   Обработал: %s%n", request.getProcessedBy());
            }
            System.out.println("   ---");
        }
    }

    private void processManagerCommand(String command) {
        switch (command) {
            case "1" -> showMyHotels();
            case "2" -> showHotelBookings();
            case "3" -> createBookingAsManager();
            case "4" -> showMyHotelRooms();
            case "5" -> manageBookingStatus();
            case "6" -> manageCancelRequests();
            case "7" -> showMyAuditLogs();
            default -> System.out.println("❌ Неизвестная команда");
        }
    }
    private void manageCancelRequests() {
        System.out.println("\n=== УПРАВЛЕНИЕ ЗАПРОСАМИ НА ОТМЕНУ ===");
        System.out.println("1. Просмотр всех запросов");
        System.out.println("2. Просмотр запросов в ожидании");
        System.out.println("3. Обработать запрос");
        System.out.print("Выберите опцию: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> showAllCancelRequests();
            case "2" -> showPendingCancelRequests();
            case "3" -> processCancelRequest();
            default -> System.out.println("❌ Неверный выбор");
        }
    }
    private void showAllCancelRequests() {
        List<CancelRequestDto> requests = cancelRequestService.getAllCancelRequests();
        if (requests.isEmpty()) {
            System.out.println("Нет запросов на отмену.");
            return;
        }

        System.out.println("\n=== ВСЕ ЗАПРОСЫ НА ОТМЕНУ ===");
        for (CancelRequestDto request : requests) {
            System.out.printf("📋 Запрос #%s [%s]%n", request.getRequestId(), request.getStatus());
            System.out.printf("   Бронирование: %s%n", request.getBookingId());
            System.out.printf("   Пользователь: %s%n", request.getUserId());
            System.out.printf("   Причина: %s%n", request.getReason());
            System.out.println("   ---");
        }
    }
    private void showPendingCancelRequests() {
        List<CancelRequestDto> requests = cancelRequestService.getCancelRequestsByStatus(CancelRequestStatus.PENDING);
        if (requests.isEmpty()) {
            System.out.println("Нет запросов в ожидании.");
            return;
        }

        System.out.println("\n=== ЗАПРОСЫ В ОЖИДАНИИ ===");
        for (CancelRequestDto request : requests) {
            System.out.printf("📋 Запрос #%s%n", request.getRequestId());
            System.out.printf("   Бронирование: %s%n", request.getBookingId());
            System.out.printf("   Пользователь: %s%n", request.getUserId());
            System.out.printf("   Причина: %s%n", request.getReason());
            System.out.printf("   Создан: %s%n", request.getCreatedAt());
            System.out.println("   ---");
        }
    }
    private void processCancelRequest() {
        System.out.print("Введите ID запроса на отмену: ");
        UUID requestId;
        try {
            requestId = UUID.fromString(scanner.nextLine().trim());
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Неверный формат ID");
            return;
        }
        try {
            CancelRequestDto request = cancelRequestService.getCancelRequest(requestId);
            if (!"PENDING".equals(request.getStatus())) {
                System.out.println("❌ Запрос уже обработан. Статус: " + request.getStatus());
                return;
            }

            System.out.println("\nИнформация о запросе:");
            System.out.println("   Бронирование: " + request.getBookingId());
            System.out.println("   Пользователь: " + request.getUserId());
            System.out.println("   Причина: " + request.getReason());
            System.out.println("   Создан: " + request.getCreatedAt());

            System.out.println("\n1. Подтвердить (APPROVED)");
            System.out.println("2. Отклонить (REJECTED)");
            System.out.print("Выберите действие: ");
            String choice = scanner.nextLine().trim();

            CancelRequestStatus status;
            if (choice.equals("1")) {
                status = CancelRequestStatus.APPROVED;
            } else if (choice.equals("2")) {
                status = CancelRequestStatus.REJECTED;
            } else {
                System.out.println("❌ Неверный выбор");
                return;
            }

            CancelRequestUpdateDto dto = new CancelRequestUpdateDto(status, currentUser.getUserId());
            CancelRequestDto updated = cancelRequestService.processCancelRequest(requestId, dto);

            System.out.println("✅ Запрос обработан! Новый статус: " + updated.getStatus());
            if (status == CancelRequestStatus.APPROVED) {
                System.out.println("   Бронирование отменено.");
            } else {
                System.out.println("   Бронирование остается активным.");
            }
        } catch (RuntimeException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }
    private void showMyAuditLogs() {
        List<AuditLog> logs = auditService.getAuditLogsByUser(currentUser.getUserId());
        if (logs.isEmpty()) {
            System.out.println("Нет записей аудита для ваших действий.");
            return;
        }

        System.out.println("\n=== МОИ ДЕЙСТВИЯ (АУДИТ) ===");
        for (AuditLog log : logs) {
            System.out.printf("📝 %s [%s]%n", log.getAction(), log.getPerformedAt());
            System.out.printf("   Сущность: %s (ID: %s)%n", log.getEntityType(), log.getEntityId());
            if (log.getDetails() != null) {
                System.out.printf("   Детали: %s%n", log.getDetails());
            }
            System.out.println("   ---");
        }
    }
    private void processAdminCommand(String command) {
        switch (command) {
            case "1" -> showAllHotels();
            case "2" -> createHotel();
            case "3" -> updateHotel();
            case "4" -> deleteOrRestoreHotel();
            case "5" -> showAllUsers();
            case "6" -> createUser();
            case "7" -> updateUser();
            case "8" -> deleteOrRestoreUser();
            case "9" -> showHotelRooms();
            case "10" -> createRoom();
            case "11" -> updateRoom();
            case "12" -> showHotelBookingsAdmin();
            case "13" -> createBookingAdmin();
            case "14" -> manageBookingStatusAdmin();
            case "15" -> showAllCancelRequestsAdmin();
            case "16" -> processCancelRequestAdmin();
            case "17" -> showAllAuditLogs();
            case "18" -> showAuditLogsByAction();
            default -> System.out.println("❌ Неизвестная команда");
        }
    }
    private void showAllCancelRequestsAdmin() {
        List<CancelRequestDto> requests = cancelRequestService.getAllCancelRequests();
        if (requests.isEmpty()) {
            System.out.println("Нет запросов на отмену.");
            return;
        }

        System.out.println("\n=== ВСЕ ЗАПРОСЫ НА ОТМЕНУ ===");
        for (CancelRequestDto request : requests) {
            System.out.printf("📋 Запрос #%s [%s]%n", request.getRequestId(), request.getStatus());
            System.out.printf("   Бронирование: %s%n", request.getBookingId());
            System.out.printf("   Пользователь: %s%n", request.getUserId());
            System.out.printf("   Причина: %s%n", request.getReason());
            System.out.printf("   Создан: %s%n", request.getCreatedAt());
            if (request.getProcessedAt() != null) {
                System.out.printf("   Обработан: %s%n", request.getProcessedAt());
                System.out.printf("   Обработал: %s%n", request.getProcessedBy());
            }
            System.out.println("   ---");
        }
    }
    private void processCancelRequestAdmin() {
        System.out.print("Введите ID запроса на отмену: ");
        UUID requestId;
        try {
            requestId = UUID.fromString(scanner.nextLine().trim());
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Неверный формат ID");
            return;
        }

        try {
            CancelRequestDto request = cancelRequestService.getCancelRequest(requestId);
            if (!"PENDING".equals(request.getStatus())) {
                System.out.println("❌ Запрос уже обработан. Статус: " + request.getStatus());
                return;
            }

            System.out.println("\nИнформация о запросе:");
            System.out.println("   Бронирование: " + request.getBookingId());
            System.out.println("   Пользователь: " + request.getUserId());
            System.out.println("   Причина: " + request.getReason());
            System.out.println("   Создан: " + request.getCreatedAt());

            System.out.println("\n1. Подтвердить (APPROVED)");
            System.out.println("2. Отклонить (REJECTED)");
            System.out.print("Выберите действие: ");
            String choice = scanner.nextLine().trim();

            CancelRequestStatus status;
            if (choice.equals("1")) {
                status = CancelRequestStatus.APPROVED;
            } else if (choice.equals("2")) {
                status = CancelRequestStatus.REJECTED;
            } else {
                System.out.println("❌ Неверный выбор");
                return;
            }

            CancelRequestUpdateDto dto = new CancelRequestUpdateDto(status, currentUser.getUserId());
            CancelRequestDto updated = cancelRequestService.processCancelRequest(requestId, dto);

            System.out.println("✅ Запрос обработан! Новый статус: " + updated.getStatus());
        } catch (RuntimeException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }
    private void showAllAuditLogs() {
        List<AuditLog> logs = auditService.getAllAuditLogs();
        if (logs.isEmpty()) {
            System.out.println("Нет записей аудита.");
            return;
        }

        System.out.println("\n=== ВСЕ АУДИТ-ЛОГИ ===");
        for (AuditLog log : logs) {
            System.out.printf("📝 %s [%s]%n", log.getAction(), log.getPerformedAt());
            System.out.printf("   Сущность: %s (ID: %s)%n", log.getEntityType(), log.getEntityId());
            System.out.printf("   Кто выполнил: %s%n", log.getPerformedBy());
            if (log.getOldValue() != null) {
                System.out.printf("   Было: %s%n", log.getOldValue());
            }
            if (log.getNewValue() != null) {
                System.out.printf("   Стало: %s%n", log.getNewValue());
            }
            if (log.getDetails() != null) {
                System.out.printf("   Детали: %s%n", log.getDetails());
            }
            System.out.println("   ---");
        }
    }
    private void showAuditLogsByAction() {
        System.out.println("\nДоступные действия:");
        for (AuditAction action : AuditAction.values()) {
            System.out.println("   " + action);
        }
        System.out.print("Введите действие: ");
        String actionStr = scanner.nextLine().trim().toUpperCase();

        try {
            AuditAction action = AuditAction.valueOf(actionStr);
            List<AuditLog> logs = auditService.getAuditLogsByAction(action);

            if (logs.isEmpty()) {
                System.out.println("Нет записей аудита для действия: " + action);
                return;
            }

            System.out.println("\n=== АУДИТ-ЛОГИ ДЛЯ " + action + " ===");
            for (AuditLog log : logs) {
                System.out.printf("📝 %s [%s]%n", log.getAction(), log.getPerformedAt());
                System.out.printf("   Сущность: %s (ID: %s)%n", log.getEntityType(), log.getEntityId());
                System.out.printf("   Кто выполнил: %s%n", log.getPerformedBy());
                if (log.getDetails() != null) {
                    System.out.printf("   Детали: %s%n", log.getDetails());
                }
                System.out.println("   ---");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Неверное действие: " + actionStr);
        }
    }

    private void updateUser() {
        System.out.print("Введите ID пользователя: ");
        UUID userId = UUID.fromString(scanner.nextLine().trim());

        System.out.println("Оставьте поле пустым, чтобы не менять.");
        System.out.print("Новое имя: ");
        String username = scanner.nextLine().trim();
        System.out.print("Новый email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Новый пароль: ");
        String password = scanner.nextLine().trim();
        System.out.print("Новая роль (CUSTOMER/MANAGER/ADMIN): ");
        String roleStr = scanner.nextLine().trim();

        UserCreateDto dto = new UserCreateDto();
        if (!username.isEmpty()) dto.setUsername(username);
        if (!email.isEmpty()) dto.setEmail(email);
        if (!password.isEmpty()) dto.setPassword(password);
        if (!roleStr.isEmpty()) {
            try {
                dto.setRole(Role.valueOf(roleStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                System.out.println("⚠️ Неверная роль, роль не изменена");
            }
        }

        try {
            UserResponseDto user = userService.updateUser(userId, dto, currentUser.getUserId());
            System.out.println("✅ Пользователь обновлен");
        } catch (RuntimeException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private void deleteOrRestoreUser() {
        System.out.print("Введите ID пользователя: ");
        UUID userId = UUID.fromString(scanner.nextLine().trim());

        System.out.println("1. Мягкое удаление");
        System.out.println("2. Восстановление");
        System.out.println("3. Полное удаление (необратимо)");
        System.out.print("Выберите действие: ");
        String choice = scanner.nextLine().trim();

        try {
            if (choice.equals("1")) {
                userService.softDeleteUser(userId, currentUser.getUserId());
                System.out.println("✅ Пользователь помечен как удаленный");
            } else if (choice.equals("2")) {
                userService.restoreUser(userId, currentUser.getUserId());
                System.out.println("✅ Пользователь восстановлен");
            } else if (choice.equals("3")) {
                userService.hardDeleteUser(userId);
                System.out.println("✅ Пользователь полностью удален");
            } else {
                System.out.println("❌ Неверный выбор");
            }
        } catch (RuntimeException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private void deleteOrRestoreHotel() {
        UUID hotelId = selectAnyHotel();
        if (hotelId == null) return;

        System.out.println("1. Мягкое удаление");
        System.out.println("2. Восстановление");
        System.out.println("3. Полное удаление (необратимо)");
        System.out.print("Выберите действие: ");
        String choice = scanner.nextLine().trim();

        try {
            if (choice.equals("1")) {
                hotelService.softDeleteHotel(hotelId, currentUser.getUserId());
                System.out.println("✅ Отель помечен как удаленный");
            } else if (choice.equals("2")) {
                hotelService.restoreHotel(hotelId, currentUser.getUserId());
                System.out.println("✅ Отель восстановлен");
            } else if (choice.equals("3")) {
                hotelService.hardDeleteHotel(hotelId);
                System.out.println("✅ Отель полностью удален");
            } else {
                System.out.println("❌ Неверный выбор");
            }
        } catch (RuntimeException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }


    private void showAllHotels() {
        List<HotelResponseDto> hotels = hotelService.getAllHotels();
        if (hotels.isEmpty()) {
            System.out.println("Нет доступных отелей.");
            return;
        }
        System.out.println("\n=== ДОСТУПНЫЕ ОТЕЛИ ===");
        for (HotelResponseDto hotel : hotels) {
            System.out.printf("🏨 %s [%s, %s]%n", hotel.getName(), hotel.getCity(), hotel.getCountry());
            System.out.printf("   ID: %s%n", hotel.getHotelId());
            System.out.printf("   Адрес: %s%n", hotel.getAddress());
            System.out.println("   ---");
        }
    }

    private void searchAvailableRooms() {
        System.out.print("Введите ID отеля: ");
        UUID hotelId = UUID.fromString(scanner.nextLine().trim());

        System.out.print("Дата заезда (ДД.ММ.ГГГГ): ");
        LocalDate checkIn = parseDate(scanner.nextLine().trim());
        System.out.print("Дата выезда (ДД.ММ.ГГГГ): ");
        LocalDate checkOut = parseDate(scanner.nextLine().trim());

        if (checkOut.isBefore(checkIn)) {
            System.out.println("❌ Дата выезда не может быть раньше даты заезда");
            return;
        }

        List<RoomResponseDto> availableRooms = getAvailableRooms(hotelId, checkIn, checkOut);

        if (availableRooms.isEmpty()) {
            System.out.println("Нет доступных комнат на выбранные даты.");
            return;
        }

        System.out.println("\n=== ДОСТУПНЫЕ КОМНАТЫ ===");
        for (RoomResponseDto room : availableRooms) {
            long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
            BigDecimal total = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));
            System.out.printf("🛏️ %s [%d мест] - $%.2f/ночь (Всего: $%.2f)%n",
                    room.getRoomType(), room.getCapacity(), room.getPricePerNight(), total);
            System.out.printf("   ID комнаты: %s%n", room.getRoomId());
            System.out.printf("   Описание: %s%n", room.getDescription());
            System.out.println("   ---");
        }
    }

    public List<RoomResponseDto> getAvailableRooms(UUID hotelId, LocalDate checkIn, LocalDate checkOut) {
        List<RoomResponseDto> allRooms = roomService.getRoomsByHotel(hotelId);
        List<RoomResponseDto> availableRooms = new ArrayList<>();

        System.out.println("=== ДИАГНОСТИКА ===");
        System.out.println("Отель ID: " + hotelId);
        System.out.println("Даты: " + checkIn + " - " + checkOut);
        System.out.println("Всего комнат в отеле: " + allRooms.size());

        // Получаем все бронирования
        List<BookingResponseDto> allBookings = bookingService.getAllBookings();
        System.out.println("Всего бронирований в системе: " + allBookings.size());

        // Фильтруем активные
        List<BookingResponseDto> activeBookings = new ArrayList<>();
        for (BookingResponseDto booking : allBookings) {
            System.out.println("Бронирование: комната=" + booking.getRoomId() +
                    ", даты=" + booking.getCheckInDate() + " - " + booking.getCheckOutDate() +
                    ", статус=" + booking.getStatus());

            if (booking.getStatus() != BookingStatus.CANCELLED &&
                    booking.getStatus() != BookingStatus.COMPLETED) {
                activeBookings.add(booking);
                System.out.println("  -> АКТИВНОЕ");
            } else {
                System.out.println("  -> ИГНОРИРУЕМ (отменено/завершено)");
            }
        }

        System.out.println("Активных бронирований: " + activeBookings.size());

        // Проверяем каждую комнату
        for (RoomResponseDto room : allRooms) {
            System.out.println("\nПроверяем комнату: " + room.getRoomId() + " (" + room.getRoomType() + ")");
            boolean isAvailable = true;

            for (BookingResponseDto booking : activeBookings) {
                boolean sameRoom = booking.getRoomId().equals(room.getRoomId());
                boolean dateOverlap = checkIn.isBefore(booking.getCheckOutDate()) &&
                        checkOut.isAfter(booking.getCheckInDate());

                System.out.println("  Бронирование: комната=" + booking.getRoomId() +
                        ", sameRoom=" + sameRoom +
                        ", dateOverlap=" + dateOverlap);

                if (sameRoom && dateOverlap) {
                    System.out.println("    -> КОНФЛИКТ! Комната занята");
                    isAvailable = false;
                    break;
                }
            }

            if (isAvailable) {
                System.out.println("  -> КОМНАТА ДОСТУПНА");
                availableRooms.add(room);
            } else {
                System.out.println("  -> КОМНАТА НЕДОСТУПНА");
            }
        }

        System.out.println("=== КОНЕЦ ДИАГНОСТИКИ ===");
        return availableRooms;
    }

    private void showMyBookings() {
        List<BookingResponseDto> bookings = bookingService.getBookingsByUser(currentUser.getUserId());
        if (bookings.isEmpty()) {
            System.out.println("У вас нет бронирований.");
            return;
        }
        System.out.println("\n=== МОИ БРОНИРОВАНИЯ ===");
        for (BookingResponseDto booking : bookings) {
            RoomResponseDto room = roomService.getRoom(booking.getRoomId());
            HotelResponseDto hotel = hotelService.getHotelsByManagerId(room.getHotelId()).stream().findFirst().orElse(null);
            System.out.printf("📅 %s - %s | Статус: %s%n",
                    booking.getCheckInDate(), booking.getCheckOutDate(), booking.getStatus());
            System.out.printf("   Сумма: $%.2f%n", booking.getTotalPrice());
            System.out.printf("   ID брони: %s%n", booking.getBookingId());
            System.out.println("   ---");
        }
    }

    private void createBookingForCustomer() {
        System.out.print("Введите ID комнаты: ");
        UUID roomId = UUID.fromString(scanner.nextLine().trim());
        System.out.print("Дата заезда (ДД.ММ.ГГГГ): ");
        LocalDate checkIn = parseDate(scanner.nextLine().trim());
        System.out.print("Дата выезда (ДД.ММ.ГГГГ): ");
        LocalDate checkOut = parseDate(scanner.nextLine().trim());

        BookingCreateDto dto = new BookingCreateDto(currentUser.getUserId(), roomId, checkIn, checkOut);
        try {
            BookingResponseDto booking = bookingService.createBooking(dto);
            System.out.println("✅ Бронирование создано! ID: " + booking.getBookingId());
            System.out.printf("💰 Общая стоимость: $%.2f%n", booking.getTotalPrice());
        } catch (RuntimeException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private void cancelMyBooking() {
        System.out.print("Введите ID бронирования для отмены: ");
        UUID bookingId = UUID.fromString(scanner.nextLine().trim());
        try {
            bookingService.cancelBooking(bookingId);
            System.out.println("✅ Бронирование отменено");
        } catch (RuntimeException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private void showMyLoyalty() {
        try {
            LoyaltyProgramResponseDto loyalty = loyaltyProgramService.getLoyaltyProgramByUser(currentUser.getUserId());
            System.out.println("\n=== ПРОГРАММА ЛОЯЛЬНОСТИ ===");
            System.out.println("🏆 Уровень: " + loyalty.getTier());
            System.out.println("⭐ Баллов: " + loyalty.getTotalPoints());
            System.out.println("💰 Всего потрачено: $" + loyalty.getTotalSpent());
            System.out.println("\nСледующий уровень:");
            if (loyalty.getTier() == LoyaltyTier.BRONZE) {
                System.out.println("   SILVER - нужно 1000 баллов");
            } else if (loyalty.getTier() == LoyaltyTier.SILVER) {
                System.out.println("   GOLD - нужно 5000 баллов");
            } else if (loyalty.getTier() == LoyaltyTier.GOLD) {
                System.out.println("   PLATINUM - нужно 10000 баллов");
            } else {
                System.out.println("   Максимальный уровень!");
            }
        } catch (RuntimeException e) {
            System.out.println("❌ Программа лояльности не найдена. Обратитесь к администратору.");
        }
    }
    private void showMyHotels() {
        List<HotelResponseDto> hotels = hotelService.getHotelsByManagerId(currentUser.getUserId());
        if (hotels.isEmpty()) {
            System.out.println("Вы не управляете ни одним отелем.");
            return;
        }
        System.out.println("\n=== МОИ ОТЕЛИ ===");
        for (HotelResponseDto hotel : hotels) {
            System.out.printf("🏨 %s [%s, %s]%n", hotel.getName(), hotel.getCity(), hotel.getCountry());
            System.out.printf("   ID: %s%n", hotel.getHotelId());
        }
    }

    private void showHotelBookings() {
        UUID hotelId = selectMyHotel();
        if (hotelId == null) return;

        List<RoomResponseDto> rooms = roomService.getRoomsByHotel(hotelId);
        System.out.println("\n=== БРОНИРОВАНИЯ ОТЕЛЯ ===");
        for (RoomResponseDto room : rooms) {
            List<BookingResponseDto> bookings = bookingService.getAllBookings().stream()
                    .filter(b -> b.getRoomId().equals(room.getRoomId()))
                    .collect(Collectors.toList());
            for (BookingResponseDto booking : bookings) {
                System.out.printf("📅 Комната %s: %s - %s [%s]%n",
                        room.getRoomType(), booking.getCheckInDate(), booking.getCheckOutDate(), booking.getStatus());
                System.out.printf("   ID брони: %s%n", booking.getBookingId());
            }
        }
    }

    private void createBookingAsManager() {
        UUID hotelId = selectMyHotel();
        if (hotelId == null) return;

        List<RoomResponseDto> rooms = roomService.getRoomsByHotel(hotelId);
        System.out.println("Доступные комнаты:");
        for (RoomResponseDto room : rooms) {
            System.out.printf("   %s - $%.2f/ночь (ID: %s)%n", room.getRoomType(), room.getPricePerNight(), room.getRoomId());
        }

        System.out.print("Введите ID пользователя (клиента): ");
        UUID userId = UUID.fromString(scanner.nextLine().trim());
        System.out.print("Введите ID комнаты: ");
        UUID roomId = UUID.fromString(scanner.nextLine().trim());
        System.out.print("Дата заезда (ДД.ММ.ГГГГ): ");
        LocalDate checkIn = parseDate(scanner.nextLine().trim());
        System.out.print("Дата выезда (ДД.ММ.ГГГГ): ");
        LocalDate checkOut = parseDate(scanner.nextLine().trim());

        BookingCreateDto dto = new BookingCreateDto(userId, roomId, checkIn, checkOut);
        try {
            BookingResponseDto booking = bookingService.createBooking(dto);
            System.out.println("✅ Бронирование создано для пользователя " + userId);
        } catch (RuntimeException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private void showMyHotelRooms() {
        UUID hotelId = selectMyHotel();
        if (hotelId == null) return;

        List<RoomResponseDto> rooms = roomService.getRoomsByHotel(hotelId);
        System.out.println("\n=== КОМНАТЫ ОТЕЛЯ ===");
        for (RoomResponseDto room : rooms) {
            System.out.printf("🛏️ %s [%d мест] - $%.2f/ночь%n",
                    room.getRoomType(), room.getCapacity(), room.getPricePerNight());
            System.out.printf("   ID: %s%n", room.getRoomId());
        }
    }

    private void manageBookingStatus() {
        UUID hotelId = selectMyHotel();
        if (hotelId == null) return;

        System.out.print("Введите ID бронирования: ");
        UUID bookingId = UUID.fromString(scanner.nextLine().trim());

        System.out.println("1. Подтвердить");
        System.out.println("2. Отменить");
        System.out.print("Выберите действие: ");
        String choice = scanner.nextLine().trim();

        try {
            if (choice.equals("1")) {
                bookingService.confirmBooking(bookingId);
                System.out.println("✅ Бронирование подтверждено");
            } else if (choice.equals("2")) {
                bookingService.cancelBooking(bookingId);
                System.out.println("✅ Бронирование отменено");
            } else {
                System.out.println("❌ Неверный выбор");
            }
        } catch (RuntimeException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private UUID selectMyHotel() {
        List<HotelResponseDto> hotels = hotelService.getHotelsByManagerId(currentUser.getUserId());
        if (hotels.isEmpty()) {
            System.out.println("Вы не управляете ни одним отелем.");
            return null;
        }
        System.out.println("Ваши отели:");
        for (int i = 0; i < hotels.size(); i++) {
            System.out.printf("%d. %s (ID: %s)%n", i + 1, hotels.get(i).getName(), hotels.get(i).getHotelId());
        }
        System.out.print("Выберите отель (номер): ");
        int index = Integer.parseInt(scanner.nextLine().trim()) - 1;
        if (index < 0 || index >= hotels.size()) {
            System.out.println("❌ Неверный выбор");
            return null;
        }
        return hotels.get(index).getHotelId();
    }

    private void createHotel() {
        System.out.print("Название отеля: ");
        String name = scanner.nextLine().trim();
        System.out.print("Город: ");
        String city = scanner.nextLine().trim();
        System.out.print("Страна: ");
        String country = scanner.nextLine().trim();
        System.out.print("Адрес: ");
        String address = scanner.nextLine().trim();
        System.out.print("ID менеджера (оставьте пустым, если пока нет): ");
        String managerIdStr = scanner.nextLine().trim();
        UUID managerId = managerIdStr.isEmpty() ? null : UUID.fromString(managerIdStr);

        HotelCreateDto dto = new HotelCreateDto(name, city, country, address, managerId);
        try {
            HotelResponseDto hotel = hotelService.createHotel(dto);
            System.out.println("✅ Отель создан! ID: " + hotel.getHotelId());
        } catch (RuntimeException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private void updateHotel() {
        UUID hotelId = selectAnyHotel();
        if (hotelId == null) return;

        System.out.println("Оставьте поле пустым, чтобы не менять.");
        System.out.print("Новое название: ");
        String name = scanner.nextLine().trim();
        System.out.print("Новый город: ");
        String city = scanner.nextLine().trim();
        System.out.print("Новая страна: ");
        String country = scanner.nextLine().trim();
        System.out.print("Новый адрес: ");
        String address = scanner.nextLine().trim();
        System.out.print("Новый ID менеджера: ");
        String managerIdStr = scanner.nextLine().trim();

        HotelUpdateDto dto = new HotelUpdateDto();
        if (!name.isEmpty()) dto.setName(name);
        if (!city.isEmpty()) dto.setCity(city);
        if (!country.isEmpty()) dto.setCountry(country);
        if (!address.isEmpty()) dto.setAddress(address);
        if (!managerIdStr.isEmpty()) dto.setManagerId(UUID.fromString(managerIdStr));

        try {
            HotelResponseDto hotel = hotelService.updateHotel(hotelId, dto);
            System.out.println("✅ Отель обновлен");
        } catch (RuntimeException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private void showAllUsers() {
        List<UserResponseDto> users = userService.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("Нет пользователей.");
            return;
        }
        System.out.println("\n=== ВСЕ ПОЛЬЗОВАТЕЛИ ===");
        for (UserResponseDto user : users) {
            System.out.printf("👤 %s [%s] - %s%n", user.getUsername(), user.getEmail(), user.getRole());
            System.out.printf("   ID: %s%n", user.getUserId());
            System.out.println("   ---");
        }
    }

    private void createUser() {
        System.out.print("Имя пользователя: ");
        String username = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Пароль: ");
        String password = scanner.nextLine().trim();
        System.out.print("Роль (CUSTOMER/MANAGER/ADMIN): ");
        String roleStr = scanner.nextLine().trim().toUpperCase();

        Role role;
        try {
            role = Role.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Неверная роль. Используется CUSTOMER по умолчанию");
            role = Role.CUSTOMER;
        }

        UserCreateDto dto = new UserCreateDto(username, email, password, role);
        try {
            UserResponseDto user = userService.createUser(dto);
            System.out.println("✅ Пользователь создан! ID: " + user.getUserId());
        } catch (RuntimeException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private void showHotelRooms() {
        UUID hotelId = selectAnyHotel();
        if (hotelId == null) return;

        List<RoomResponseDto> rooms = roomService.getRoomsByHotel(hotelId);
        System.out.println("\n=== КОМНАТЫ ОТЕЛЯ ===");
        for (RoomResponseDto room : rooms) {
            System.out.printf("🛏️ %s [%d мест] - $%.2f/ночь%n",
                    room.getRoomType(), room.getCapacity(), room.getPricePerNight());
            System.out.printf("   ID: %s%n", room.getRoomId());
            System.out.printf("   Описание: %s%n", room.getDescription());
        }
    }

    private void createRoom() {
        UUID hotelId = selectAnyHotel();
        if (hotelId == null) return;

        System.out.print("Тип комнаты (SINGLE/DOUBLE/SUITE/DELUXE): ");
        RoomType roomType = RoomType.valueOf(scanner.nextLine().trim().toUpperCase());
        System.out.print("Цена за ночь: ");
        BigDecimal price = new BigDecimal(scanner.nextLine().trim());
        System.out.print("Вместимость (количество гостей): ");
        int capacity = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Описание: ");
        String description = scanner.nextLine().trim();
        System.out.print("Площадь (м²): ");
        double area = Double.parseDouble(scanner.nextLine().trim());

        RoomCreateDto dto = new RoomCreateDto(hotelId, roomType, price, capacity, description, null, area);
        try {
            RoomResponseDto room = roomService.createRoom(dto);
            System.out.println("✅ Номер создан! ID: " + room.getRoomId());
        } catch (RuntimeException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private void updateRoom() {
        System.out.print("Введите ID комнаты: ");
        UUID roomId = UUID.fromString(scanner.nextLine().trim());

        System.out.println("Оставьте поле пустым, чтобы не менять.");
        System.out.print("Новый тип комнаты (SINGLE/DOUBLE/SUITE/DELUXE): ");
        String typeStr = scanner.nextLine().trim();
        System.out.print("Новая цена за ночь: ");
        String priceStr = scanner.nextLine().trim();
        System.out.print("Новая вместимость: ");
        String capacityStr = scanner.nextLine().trim();
        System.out.print("Новое описание: ");
        String description = scanner.nextLine().trim();

        RoomUpdateDto dto = new RoomUpdateDto();
        if (!typeStr.isEmpty()) dto.setRoomType(RoomType.valueOf(typeStr.toUpperCase()));
        if (!priceStr.isEmpty()) dto.setPricePerNight(new BigDecimal(priceStr));
        if (!capacityStr.isEmpty()) dto.setCapacity(Integer.parseInt(capacityStr));
        if (!description.isEmpty()) dto.setDescription(description);

        try {
            RoomResponseDto room = roomService.updateRoom(roomId, dto);
            System.out.println("✅ Номер обновлен");
        } catch (RuntimeException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private void showHotelBookingsAdmin() {
        UUID hotelId = selectAnyHotel();
        if (hotelId == null) return;

        List<RoomResponseDto> rooms = roomService.getRoomsByHotel(hotelId);
        System.out.println("\n=== БРОНИРОВАНИЯ ОТЕЛЯ ===");
        for (RoomResponseDto room : rooms) {
            List<BookingResponseDto> bookings = bookingService.getAllBookings().stream()
                    .filter(b -> b.getRoomId().equals(room.getRoomId()))
                    .collect(Collectors.toList());
            for (BookingResponseDto booking : bookings) {
                System.out.printf("📅 Комната %s: %s - %s [%s]%n",
                        room.getRoomType(), booking.getCheckInDate(), booking.getCheckOutDate(), booking.getStatus());
            }
        }
    }

    private void createBookingAdmin() {
        System.out.print("Введите ID пользователя: ");
        UUID userId = UUID.fromString(scanner.nextLine().trim());
        System.out.print("Введите ID комнаты: ");
        UUID roomId = UUID.fromString(scanner.nextLine().trim());
        System.out.print("Дата заезда (ДД.ММ.ГГГГ): ");
        LocalDate checkIn = parseDate(scanner.nextLine().trim());
        System.out.print("Дата выезда (ДД.ММ.ГГГГ): ");
        LocalDate checkOut = parseDate(scanner.nextLine().trim());

        BookingCreateDto dto = new BookingCreateDto(userId, roomId, checkIn, checkOut);
        try {
            BookingResponseDto booking = bookingService.createBooking(dto);
            System.out.println("✅ Бронирование создано! ID: " + booking.getBookingId());
        } catch (RuntimeException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private void manageBookingStatusAdmin() {
        System.out.print("Введите ID бронирования: ");
        UUID bookingId = UUID.fromString(scanner.nextLine().trim());

        System.out.println("1. Подтвердить");
        System.out.println("2. Отменить");
        System.out.print("Выберите действие: ");
        String choice = scanner.nextLine().trim();

        try {
            if (choice.equals("1")) {
                bookingService.confirmBooking(bookingId);
                System.out.println("✅ Бронирование подтверждено");
            } else if (choice.equals("2")) {
                bookingService.cancelBooking(bookingId);
                System.out.println("✅ Бронирование отменено");
            } else {
                System.out.println("❌ Неверный выбор");
            }
        } catch (RuntimeException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private UUID selectAnyHotel() {
        List<HotelResponseDto> hotels = hotelService.getAllHotels();
        if (hotels.isEmpty()) {
            System.out.println("Нет доступных отелей.");
            return null;
        }
        System.out.println("Все отели:");
        for (int i = 0; i < hotels.size(); i++) {
            System.out.printf("%d. %s [%s] (ID: %s)%n",
                    i + 1, hotels.get(i).getName(), hotels.get(i).getCity(), hotels.get(i).getHotelId());
        }
        System.out.print("Выберите отель (номер): ");
        int index = Integer.parseInt(scanner.nextLine().trim()) - 1;
        if (index < 0 || index >= hotels.size()) {
            System.out.println("❌ Неверный выбор");
            return null;
        }
        return hotels.get(index).getHotelId();
    }

    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, dateFormatter);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Неверный формат даты. Используйте ДД.ММ.ГГГГ");
        }
    }
}