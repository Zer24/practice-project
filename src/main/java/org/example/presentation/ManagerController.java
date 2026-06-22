package org.example.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.AuditLog;
import org.example.domain.enums.CancelRequestStatus;
import org.example.dto.*;
import org.example.service.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ManagerController {

    private final ConsoleScanner scanner;
    private final HotelService hotelService;
    private final RoomService roomService;
    private final BookingService bookingService;
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
        System.out.println("ГЛАВНОЕ МЕНЮ [Роль: MANAGER]");
        System.out.println("═══════════════════════════════════════");
        System.out.println("1. Мои отели");
        System.out.println("2. Бронирования моего отеля");
        System.out.println("3. Создать бронирование для клиента");
        System.out.println("4. Просмотр комнат моего отеля");
        System.out.println("5. Подтвердить/отменить бронирование");
        System.out.println("6. Управление запросами на отмену");
        System.out.println("7. Просмотр аудита (мои действия)");
        System.out.println("\nlogout - выйти из аккаунта");
        System.out.println("exit - завершить работу");
        System.out.print("\n> ");
    }

    private void processCommand(String command) {
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

    private void showMyHotels() {
        List<HotelResponseDto> hotels = hotelService.getHotelsByManagerId(currentUser.userId());
        if (hotels.isEmpty()) {
            System.out.println("Вы не управляете ни одним отелем.");
            return;
        }
        System.out.println("\n=== МОИ ОТЕЛИ ===");
        for (HotelResponseDto hotel : hotels) {
            System.out.printf("🏨 %s [%s, %s]%n", hotel.name(), hotel.city(), hotel.country());
            System.out.printf("   ID: %s%n", hotel.hotelId());
        }
    }

    private void showHotelBookings() {
        UUID hotelId = selectMyHotel();
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

    private void createBookingAsManager() {
        UUID hotelId = selectMyHotel();
        if (hotelId == null) return;

        List<RoomResponseDto> rooms = roomService.getRoomsByHotel(hotelId);
        System.out.println("Доступные комнаты:");
        for (RoomResponseDto room : rooms) {
            System.out.printf("   %s - $%.2f/ночь (ID: %s)%n", room.roomType(), room.pricePerNight(), room.roomId());
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
            System.out.printf("📋 Запрос #%s [%s]%n", request.requestId(), request.status());
            System.out.printf("   Бронирование: %s%n", request.bookingId());
            System.out.printf("   Пользователь: %s%n", request.userId());
            System.out.printf("   Причина: %s%n", request.reason());
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
            System.out.printf("📋 Запрос #%s%n", request.requestId());
            System.out.printf("   Бронирование: %s%n", request.bookingId());
            System.out.printf("   Пользователь: %s%n", request.userId());
            System.out.printf("   Причина: %s%n", request.reason());
            System.out.printf("   Создан: %s%n", request.createdAt());
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
        List<AuditLog> logs = auditService.getAuditLogsByUser(currentUser.userId());
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

    private UUID selectMyHotel() {
        List<HotelResponseDto> hotels = hotelService.getHotelsByManagerId(currentUser.userId());
        if (hotels.isEmpty()) {
            System.out.println("Вы не управляете ни одним отелем.");
            return null;
        }
        System.out.println("Ваши отели:");
        for (int i = 0; i < hotels.size(); i++) {
            System.out.printf("%d. %s (ID: %s)%n", i + 1, hotels.get(i).name(), hotels.get(i).hotelId());
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