package org.example.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.AuditLog;
import org.example.domain.enums.AuditAction;
import org.example.domain.enums.CancelRequestStatus;
import org.example.domain.enums.Role;
import org.example.domain.enums.RoomType;
import org.example.dto.*;
import org.example.service.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final ConsoleScanner scanner;
    private final HotelService hotelService;
    private final BookingService bookingService;
    private final RoomService roomService;
    private final UserService userService;
    private final CancelRequestService cancelRequestService;
    private final AuditService auditService;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private UserResponseDto currentUser;

    public void start(UserResponseDto user) {
        currentUser = user;
        while (true) {
            try {
                showMenu();
                String command = scanner.nextLine().trim();

                if (command.equalsIgnoreCase("logout")) {
                    System.out.println("Вы вышли из системы.\n");
                    break;
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

    private void showMenu() {
        System.out.println("\n═══════════════════════════════════════");
        System.out.println("ГЛАВНОЕ МЕНЮ [Роль: ADMIN]");
        System.out.println("═══════════════════════════════════════");
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
        System.out.println("\nlogout - выйти из аккаунта");
        System.out.println("exit - завершить работу");
        System.out.print("\n> ");
    }

    private void processCommand(String command) {
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

    private void showAllHotels() {
        List<HotelResponseDto> hotels = hotelService.getAllHotels();
        if (hotels.isEmpty()) {
            System.out.println("Нет доступных отелей.");
            return;
        }
        System.out.println("\n=== ДОСТУПНЫЕ ОТЕЛИ ===");
        for (HotelResponseDto hotel : hotels) {
            System.out.printf("🏨 %s [%s, %s]%n", hotel.name(), hotel.city(), hotel.country());
            System.out.printf("   ID: %s%n", hotel.hotelId());
            System.out.printf("   Адрес: %s%n", hotel.address());
            System.out.println("   ---");
        }
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
            System.out.println("✅ Отель создан! ID: " + hotel.hotelId());
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

        HotelUpdateDto dto = new HotelUpdateDto(
                name.isBlank() ? null : name,
                city.isBlank() ? null : city,
                country.isBlank() ? null : country,
                address.isBlank() ? null : address,
                null,
                managerIdStr.isBlank() ? null : UUID.fromString(managerIdStr)
        );

        try {
            HotelResponseDto hotel = hotelService.updateHotel(hotelId, dto);
            System.out.println("✅ Отель обновлен");
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
            switch (choice) {
                case "1" -> {
                    hotelService.softDeleteHotel(hotelId, currentUser.userId());
                    System.out.println("✅ Отель помечен как удаленный");
                }
                case "2" -> {
                    hotelService.restoreHotel(hotelId, currentUser.userId());
                    System.out.println("✅ Отель восстановлен");
                }
                case "3" -> {
                    hotelService.hardDeleteHotel(hotelId);
                    System.out.println("✅ Отель полностью удален");
                }
                default -> System.out.println("❌ Неверный выбор");
            }
        } catch (RuntimeException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private void showAllUsers() {
        int page = 0;
        int size = 10;
        boolean hasMorePages = true;

        while (hasMorePages) {
            Pageable pageable = PageRequest.of(page, size, Sort.by("username").ascending());
            Page<UserResponseDto> userPage = userService.getAllUsers(pageable);

            if (userPage.isEmpty()) {
                if (page == 0) {
                    System.out.println("Нет пользователей.");
                }
                break;
            }

            System.out.printf("\n=== ВСЕ ПОЛЬЗОВАТЕЛИ (Страница %d из %d) ===%n",
                    page + 1, userPage.getTotalPages());
            System.out.printf("Всего: %d, Показано: %d%n",
                    userPage.getTotalElements(), userPage.getNumberOfElements());
            System.out.println("---");

            for (UserResponseDto user : userPage.getContent()) {
                System.out.printf("👤 %s [%s] - %s%n", user.username(), user.email(), user.role());
                System.out.printf("   ID: %s%n", user.userId());
                System.out.println("   ---");
            }

            hasMorePages = showPaginationMenu(userPage, page);
            if (hasMorePages) {
                page = updatePage(page, userPage);
            }
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
            System.out.println("✅ Пользователь создан! ID: " + user.userId());
        } catch (RuntimeException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
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

        UserCreateDto dto = new UserCreateDto(
                username.isEmpty() ? null : username,
                email.isEmpty() ? null : email,
                password.isEmpty() ? null : password,
                roleStr.isEmpty() ? null : Role.valueOf(roleStr.toUpperCase())
        );

        try {
            UserResponseDto user = userService.updateUser(userId, dto, currentUser.userId());
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
                userService.softDeleteUser(userId, currentUser.userId());
                System.out.println("✅ Пользователь помечен как удаленный");
            } else if (choice.equals("2")) {
                userService.restoreUser(userId, currentUser.userId());
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

    private void showHotelRooms() {
        UUID hotelId = selectAnyHotel();
        if (hotelId == null) return;

        int page = 0;
        int size = 10;
        boolean hasMorePages = true;

        while (hasMorePages) {
            Pageable pageable = PageRequest.of(page, size, Sort.by("roomType").ascending());
            Page<RoomResponseDto> roomPage = roomService.getRoomsByHotel(hotelId, pageable);

            if (roomPage.isEmpty()) {
                if (page == 0) {
                    System.out.println("Нет комнат в этом отеле.");
                }
                break;
            }

            System.out.printf("\n=== КОМНАТЫ ОТЕЛЯ (Страница %d из %d) ===%n",
                    page + 1, roomPage.getTotalPages());
            System.out.printf("Всего: %d, Показано: %d%n",
                    roomPage.getTotalElements(), roomPage.getNumberOfElements());
            System.out.println("---");

            for (RoomResponseDto room : roomPage.getContent()) {
                System.out.printf("🛏️ %s [%d мест] - $%.2f/ночь%n",
                        room.roomType(), room.capacity(), room.pricePerNight());
                System.out.printf("   ID: %s%n", room.roomId());
                System.out.printf("   Описание: %s%n", room.description());
                System.out.println("   ---");
            }

            hasMorePages = showPaginationMenu(roomPage, page);
            if (hasMorePages) {
                page = updatePage(page, roomPage);
            }
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
            System.out.println("✅ Номер создан! ID: " + room.roomId());
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

        RoomUpdateDto dto = new RoomUpdateDto(
                typeStr.isBlank() ? null : RoomType.valueOf(typeStr.toUpperCase()),
                priceStr.isBlank() ? null : new BigDecimal(priceStr),
                capacityStr.isBlank() ? null : Integer.parseInt(capacityStr),
                description.isBlank() ? null : description,
                null,
                null
        );

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

        int page = 0;
        int size = 10;
        boolean hasMorePages = true;

        while (hasMorePages) {
            Pageable pageable = PageRequest.of(page, size, Sort.by("checkInDate").descending());
            Page<BookingResponseDto> bookingPage = bookingService.getBookingsByHotel(hotelId, pageable);

            if (bookingPage.isEmpty()) {
                if (page == 0) {
                    System.out.println("Нет бронирований в этом отеле.");
                }
                break;
            }

            System.out.printf("\n=== БРОНИРОВАНИЯ ОТЕЛЯ (Страница %d из %d) ===%n",
                    page + 1, bookingPage.getTotalPages());
            System.out.printf("Всего: %d, Показано: %d%n",
                    bookingPage.getTotalElements(), bookingPage.getNumberOfElements());
            System.out.println("---");

            for (BookingResponseDto booking : bookingPage.getContent()) {
                RoomResponseDto room = roomService.getRoom(booking.roomId());
                System.out.printf("📅 Комната %s: %s - %s [%s]%n",
                        room.roomType(), booking.checkInDate(), booking.checkOutDate(), booking.status());
                System.out.printf("   ID брони: %s%n", booking.bookingId());
                System.out.println("   ---");
            }

            hasMorePages = showPaginationMenu(bookingPage, page);
            if (hasMorePages) {
                page = updatePage(page, bookingPage);
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
            System.out.println("✅ Бронирование создано! ID: " + booking.bookingId());
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

    private void showAllCancelRequestsAdmin() {
        int page = 0;
        int size = 10;
        boolean hasMorePages = true;

        while (hasMorePages) {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<CancelRequestDto> requestPage = cancelRequestService.getAllCancelRequests(pageable);

            if (requestPage.isEmpty()) {
                if (page == 0) {
                    System.out.println("Нет запросов на отмену.");
                }
                break;
            }

            System.out.printf("\n=== ВСЕ ЗАПРОСЫ НА ОТМЕНУ (Страница %d из %d) ===%n",
                    page + 1, requestPage.getTotalPages());
            System.out.printf("Всего: %d, Показано: %d%n",
                    requestPage.getTotalElements(), requestPage.getNumberOfElements());
            System.out.println("---");

            for (CancelRequestDto request : requestPage.getContent()) {
                System.out.printf("📋 Запрос #%s [%s]%n", request.requestId(), request.status());
                System.out.printf("   Бронирование: %s%n", request.bookingId());
                System.out.printf("   Пользователь: %s%n", request.userId());
                System.out.printf("   Причина: %s%n", request.reason());
                System.out.printf("   Создан: %s%n", request.createdAt());
                if (request.processedAt() != null) {
                    System.out.printf("   Обработан: %s%n", request.processedAt());
                    System.out.printf("   Обработал: %s%n", request.processedBy());
                }
                System.out.println("   ---");
            }

            hasMorePages = showPaginationMenu(requestPage, page);
            if (hasMorePages) {
                page = updatePage(page, requestPage);
            }
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
            if (!"PENDING".equals(request.status())) {
                System.out.println("❌ Запрос уже обработан. Статус: " + request.status());
                return;
            }

            System.out.println("\nИнформация о запросе:");
            System.out.println("   Бронирование: " + request.bookingId());
            System.out.println("   Пользователь: " + request.userId());
            System.out.println("   Причина: " + request.reason());
            System.out.println("   Создан: " + request.createdAt());

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

            CancelRequestUpdateDto dto = new CancelRequestUpdateDto(status, currentUser.userId());
            CancelRequestDto updated = cancelRequestService.processCancelRequest(requestId, dto);

            System.out.println("✅ Запрос обработан! Новый статус: " + updated.status());
        } catch (RuntimeException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private void showAllAuditLogs() {
        int page = 0;
        int size = 10;
        boolean hasMorePages = true;

        while (hasMorePages) {
            Pageable pageable = PageRequest.of(page, size, Sort.by("performedAt").descending());
            Page<AuditLog> logPage = auditService.getAllAuditLogs(pageable);

            if (logPage.isEmpty()) {
                if (page == 0) {
                    System.out.println("Нет записей аудита.");
                }
                break;
            }

            System.out.printf("\n=== ВСЕ АУДИТ-ЛОГИ (Страница %d из %d) ===%n",
                    page + 1, logPage.getTotalPages());
            System.out.printf("Всего: %d, Показано: %d%n",
                    logPage.getTotalElements(), logPage.getNumberOfElements());
            System.out.println("---");

            for (AuditLog log : logPage.getContent()) {
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

            hasMorePages = showPaginationMenu(logPage, page);
            if (hasMorePages) {
                page = updatePage(page, logPage);
            }
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

            int page = 0;
            int size = 10;
            boolean hasMorePages = true;

            while (hasMorePages) {
                Pageable pageable = PageRequest.of(page, size, Sort.by("performedAt").descending());
                Page<AuditLog> logPage = auditService.getAuditLogsByAction(action, pageable);

                if (logPage.isEmpty()) {
                    if (page == 0) {
                        System.out.println("Нет записей аудита для действия: " + action);
                    }
                    break;
                }

                System.out.printf("\n=== АУДИТ-ЛОГИ ДЛЯ %s (Страница %d из %d) ===%n",
                        action, page + 1, logPage.getTotalPages());
                System.out.printf("Всего: %d, Показано: %d%n",
                        logPage.getTotalElements(), logPage.getNumberOfElements());
                System.out.println("---");

                for (AuditLog log : logPage.getContent()) {
                    System.out.printf("📝 %s [%s]%n", log.getAction(), log.getPerformedAt());
                    System.out.printf("   Сущность: %s (ID: %s)%n", log.getEntityType(), log.getEntityId());
                    System.out.printf("   Кто выполнил: %s%n", log.getPerformedBy());
                    if (log.getDetails() != null) {
                        System.out.printf("   Детали: %s%n", log.getDetails());
                    }
                    System.out.println("   ---");
                }

                hasMorePages = showPaginationMenu(logPage, page);
                if (hasMorePages) {
                    page = updatePage(page, logPage);
                }
            }
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Неверное действие: " + actionStr);
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
                    i + 1, hotels.get(i).name(), hotels.get(i).city(), hotels.get(i).hotelId());
        }
        System.out.print("Выберите отель (номер): ");
        int index = Integer.parseInt(scanner.nextLine().trim()) - 1;
        if (index < 0 || index >= hotels.size()) {
            System.out.println("❌ Неверный выбор");
            return null;
        }
        return hotels.get(index).hotelId();
    }

    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, dateFormatter);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Неверный формат даты. Используйте ДД.ММ.ГГГГ");
        }
    }
    private <T> boolean showPaginationMenu(Page<T> page, int currentPage) {
        if (!page.hasNext() && currentPage == 0) {
            return false;
        }

        System.out.print("Нажмите 'n' для следующей страницы, 'p' для предыдущей, 'q' для выхода: ");
        String input = scanner.nextLine().trim().toLowerCase();

        return !input.equals("q");
    }

    private int updatePage(int currentPage, Page<?> page) {
        System.out.print("Выберите страницу (n/p): ");
        String input = scanner.nextLine().trim().toLowerCase();

        if (input.equals("n") && page.hasNext()) {
            return currentPage + 1;
        } else if (input.equals("p") && currentPage > 0) {
            return currentPage - 1;
        }
        return currentPage;
    }
}