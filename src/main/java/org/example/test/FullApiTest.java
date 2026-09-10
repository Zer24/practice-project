package org.example.test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.example.test.ApiTestUtils.*;

public class FullApiTest {

    private static String adminToken;
    private static String managerToken;
    private static String customerToken;

    private static UUID managerUserId;
    private static UUID customerUserId;
    private static UUID secondCustomerUserId;

    private static UUID hotelId;
    private static UUID roomId;
    private static UUID bookingId;
    private static UUID cancelRequestId;

    private static String customerUsername;

    public static void main(String[] args) {
        try {
            testAuthentication();

            testUserManagement();

            testHotelManagement();

            testRoomManagement();

            testBookingManagement();

            testCustomerFunctionality();

            testManagerFunctionality();

            testIntegrationScenarios();

            testSecurity();

            deleteHotel(adminToken, createTestHotel(adminToken, "123123"));
            System.out.println("Журнал администратора");
            for(Map<String, Object> map:getAuditLogs(adminToken, 0, 10)){
                System.out.println(map.get("action")+" "+map.get("entityType")+" "+map.get("oldValue")+" "+map.get("newValue"));
            }

            cleanup();
        } catch (Exception e) {
            System.err.println("КРИТИЧЕСКАЯ ОШИБКА: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testAuthentication() {
        System.out.println("1. Тестирование входа и регистрации");

        System.out.println("\n1.1 Регистрация");

        RegistrationResult customer = createTestUser();
        if (customer != null) {
            customerUserId = customer.getUserId();
            customerUsername = customer.getUsername();
            customerToken = customer.getAccessToken();
            System.out.println("Успешная регистрация пользователя CUSTOMER");
        }

        System.out.println("\n Регистрация с существующим username");
        Map<String, Object> duplicateUsername = new HashMap<>();
        duplicateUsername.put("username", customerUsername);
        duplicateUsername.put("email", "unique_" + System.currentTimeMillis() + "@example.com");
        duplicateUsername.put("password", "Test1234!");
        duplicateUsername.put("phone", "+375291234567");
        duplicateUsername.put("firstName", "Дубликат");
        duplicateUsername.put("lastName", "Username");
        RegistrationResult duplicateUser = registerUser(duplicateUsername);
        if (duplicateUser == null) {
            System.out.println("Дублирование username обнаружено (409 Conflict)");
        }

        System.out.println("\n Регистрация с некорректными данными");
        testInvalidRegistration();

        System.out.println("\n1.2 Логин");

        AuthResult authResult = login(customerUsername, "Test1234!");
        if (authResult != null) {
            System.out.println("Успешный логин, получены токены");
            customerToken = authResult.getAccessToken();
        }

        System.out.println("Логин с неверным паролем");
        AuthResult failedLogin = login(customerUsername, "WrongPassword123!");
        if (failedLogin == null) {
            System.out.println("Неверный пароль отклонен (401 Unauthorized)");
        }

        System.out.println("\n1.3 Логин администратора и менеджера");

        adminToken = loginAdmin();
        if (adminToken != null) {
            System.out.println("Администратор авторизован");
        }

        System.out.println("\n Создание пользователя с ролью MANAGER");
        RegistrationResult manager = createTestUser();
        if (manager != null) {
            managerUserId = manager.getUserId();
            String managerUsername = manager.getUsername();
            changeUserRole(adminToken, managerUserId, "MANAGER");
            managerToken = login(managerUsername, "Test1234!").getAccessToken();
            System.out.println("Менеджер создан и авторизован");
        }

        System.out.println("\n1.4 Обновление токенов");
        testRefreshToken();
    }

    private static void testInvalidRegistration() {
        Map<String, Object> shortPassword = new HashMap<>();
        shortPassword.put("username", "short_" + System.currentTimeMillis());
        shortPassword.put("email", "short_" + System.currentTimeMillis() + "@example.com");
        shortPassword.put("password", "123");
        shortPassword.put("phone", "+375291234573");
        shortPassword.put("firstName", "Короткий");
        shortPassword.put("lastName", "Пароль");
        RegistrationResult result = registerUser(shortPassword);
        if (result == null) {
            System.out.println("  Короткий пароль отклонен (400 Bad Request)");
        }

        Map<String, Object> invalidEmail = new HashMap<>();
        invalidEmail.put("username", "invalid_" + System.currentTimeMillis());
        invalidEmail.put("email", "notanemail");
        invalidEmail.put("password", "Test1234!");
        invalidEmail.put("phone", "+375291234574");
        invalidEmail.put("firstName", "Неверный");
        invalidEmail.put("lastName", "Email");
        result = registerUser(invalidEmail);
        if (result == null) {
            System.out.println("  Неверный email отклонен (400 Bad Request)");
        }

        Map<String, Object> missingFields = new HashMap<>();
        missingFields.put("username", "missing_" + System.currentTimeMillis());
        missingFields.put("password", "Test1234!");
        result = registerUser(missingFields);
        if (result == null) {
            System.out.println("  Отсутствие обязательных полей обнаружено (400 Bad Request)");
        }
    }

    private static void testRefreshToken() {
        RegistrationResult user = createTestUser();
        if (user == null) {
            System.out.println("Не удалось создать тестового пользователя");
            return;
        }

        String accessToken = user.getAccessToken();
        String refreshToken = user.getRefreshToken();

        System.out.println("Пользователь создан: " + user.getUsername());
        System.out.println("Access токен: " + (accessToken != null ? accessToken.substring(0, 20) + "..." : "null"));
        System.out.println("Refresh токен: " + (refreshToken != null ? refreshToken.substring(0, 20) + "..." : "null"));

        boolean isAccessValid = validateToken(accessToken);
        System.out.println("  Access токен валиден: " + (isAccessValid ? "y" : "n"));

        if (!isAccessValid) {
            System.out.println("Токены не валидны - тест прерван");
            cleanupTestData(loginAdmin(), null, null, null, null, user.getUserId());
            return;
        }

        // 3. Обновляем токены
        System.out.println("\nОбновление токенов...");
        AuthResult newTokens = refreshTokens(refreshToken);

        if (newTokens == null) {
            System.out.println("Не удалось обновить токены");
            cleanupTestData(loginAdmin(), null, null, null, null, user.getUserId());
            return;
        }

        String newAccessToken = newTokens.getAccessToken();
        String newRefreshToken = newTokens.getRefreshToken();

        System.out.println("Новый access токен: " + (newAccessToken != null ? newAccessToken.substring(0, 20) + "..." : "null"));
        System.out.println("Новый refresh токен: " + (newRefreshToken != null ? newRefreshToken.substring(0, 20) + "..." : "null"));

        // 4. Проверяем, что новые токены валидны
        System.out.println("\nПроверка валидности токенов:");
        boolean isNewAccessValid = validateToken(newAccessToken);
        isAccessValid = validateToken(accessToken);
        System.out.println("  Новый access токен валиден: " + (isNewAccessValid ? "y" : "n"));
        System.out.println("  Старый access токен валиден: " + (isAccessValid ? "y" : "n"));

        // 5. Проверяем, что старый refresh токен больше не работает
        System.out.println("\nПроверка, что старый refresh токен недействителен:");
        boolean isOldRefreshValid = isRefreshTokenValid(refreshToken);
        System.out.println("Старый refresh токен все еще валиден: " + (isOldRefreshValid ? "y" : "n"));

        System.out.println("Инвалидация токена: "+(logout(newRefreshToken) ? "y" : "n"));
        boolean isNewRefreshValid = isRefreshTokenValid(newRefreshToken);
        System.out.println("Новый refresh токен все еще валиден: " + (isNewRefreshValid ? "y" : "n"));

        // 8. Очистка
        System.out.println("\nОчистка тестовых данных...");
        cleanupTestData(loginAdmin(), null, null, null, null, user.getUserId());
    }
    private static void testUserManagement() {
        System.out.println("2. Тестирование пользователей");

        System.out.println("\n2.1 Получение списка пользователей");

        var allUsers = getAllUsers(adminToken);
        if (allUsers != null) {
            System.out.println("Получен список пользователей, всего: " + allUsers.size());
        }

        var usersByUsername = getUsersByUsername(adminToken, customerUsername);
        if (usersByUsername != null && !usersByUsername.isEmpty()) {
            System.out.println("Фильтрация по username работает");
        }

        System.out.println("\n2.2 Изменение роли пользователя");

        RegistrationResult roleTestUser = createTestUser();
        if (roleTestUser != null) {
            System.out.println("Создан тестовый пользователь для смены роли");
            boolean roleChanged = changeUserRole(adminToken, roleTestUser.getUserId(), "MANAGER");
            if (roleChanged) {
                System.out.println("Роль успешно изменена на MANAGER");
            }

            System.out.println("Попытка изменения на несуществующую роль");
            boolean invalidRole = changeUserRole(adminToken, roleTestUser.getUserId(), "INVALID_ROLE");
            if (!invalidRole) {
                System.out.println("Несуществующая роль отклонена (400 Bad Request)");
            }

            softDeleteUser(adminToken, roleTestUser.getUserId());
        }

        System.out.println("\n2.3 Мягкое удаление пользователя");

        RegistrationResult softDeleteUser = createTestUser();
        if (softDeleteUser != null) {
            UUID userId = softDeleteUser.getUserId();
            System.out.println("Создан пользователь для мягкого удаления: " + userId);

            boolean deleted = softDeleteUser(adminToken, userId);
            if (deleted) {
                System.out.println("Пользователь мягко удален");

                var usersAfterDelete = getUsersByUsername(adminToken, softDeleteUser.getUsername());
                if (usersAfterDelete == null || usersAfterDelete.isEmpty()) {
                    System.out.println("Пользователь не отображается в списке после удаления");
                }

                boolean restored = restoreUser(adminToken, userId);
                if (restored) {
                    System.out.println("Пользователь восстановлен");

                    var usersAfterRestore = getUsersByUsername(adminToken, softDeleteUser.getUsername());
                    if (usersAfterRestore != null && !usersAfterRestore.isEmpty()) {
                        System.out.println("Пользователь снова отображается в списке");
                    }
                }

                softDeleteUser(adminToken, userId);
            }
        }

        System.out.println("\n2.4 Проверка доступа к админским эндпоинтам");

        if (customerToken != null) {
            var usersAsCustomer = getAllUsers(customerToken);
            if (usersAsCustomer == null) {
                System.out.println("CUSTOMER не может получить список пользователей (403 Forbidden)");
            }

            boolean roleChangeAsCustomer = changeUserRole(customerToken, UUID.randomUUID(), "MANAGER");
            if (!roleChangeAsCustomer) {
                System.out.println("CUSTOMER не может изменить роль (403 Forbidden)");
            }
        }
    }

    private static void testHotelManagement() {
        System.out.println("3. Тестирование отелей");

        System.out.println("\n3.1 Получение списка отелей (публичный)");

        var hotels = getHotelsWithFilters(customerToken, null, null, null, null, 0, 10);
        if (hotels != null) {
            System.out.println("Доступ к списку отелей работает");
        }

        var hotelsByCity = getHotelsWithFilters(customerToken, "Минск", null, null, null, 0, 10);
        if (hotelsByCity != null) {
            System.out.println("Фильтрация по городу работает");
        }

        System.out.println("\n3.2 Создание отеля");

        hotelId = createTestHotel(adminToken, "Тестовый Отель_" + System.currentTimeMillis());
        if (hotelId != null) {
            System.out.println("ADMIN создал отель: " + hotelId);
        }

        if (managerToken != null) {
            UUID managerHotelId = createTestHotel(managerToken, "Отель Менеджера_" + System.currentTimeMillis());
            if (managerHotelId != null) {
                System.out.println("MANAGER создал отель: " + managerHotelId);
                deleteHotel(adminToken, managerHotelId);
            }
        }

        if (customerToken != null) {
            UUID customerHotelId = createTestHotel(customerToken, "Отель Клиента_" + System.currentTimeMillis());
            if (customerHotelId == null) {
                System.out.println("CUSTOMER не может создать отель (403 Forbidden)");
            } else {
                deleteHotel(adminToken, customerHotelId);
            }
        }

        System.out.println("\n3.3 Получение отеля по ID");

        if (hotelId != null) {
            var hotel = getHotel(customerToken, hotelId);
            if (hotel != null) {
                System.out.println("Отель получен по ID: " + hotel.get("name"));
            }

            var nonExistentHotel = getHotel(customerToken, UUID.randomUUID());
            if (nonExistentHotel == null) {
                System.out.println("Несуществующий отель не найден (404 Not Found)");
            }
        }

        System.out.println("\n3.4 Обновление отеля");

        if (hotelId != null && adminToken != null) {
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("name", "Обновленный Отель_" + System.currentTimeMillis());
            updateData.put("city", "Гродно");
            updateData.put("address", "ул. Обновленная, 10");
            updateData.put("rating", 4.8);

            var updatedHotel = updateHotel(adminToken, hotelId, updateData);
            if (updatedHotel != null) {
                System.out.println("ADMIN обновил отель: " + updatedHotel.get("name"));
            }
        }
    }

    private static void testRoomManagement() {
        System.out.println("4. Тестирование комнат");

        if (hotelId == null) {
            System.out.println(" Нет отеля для тестирования комнат");
            return;
        }

        System.out.println("\n4.1 Получение комнат отеля (публичный)");

        var rooms = getRoomsByHotel(customerToken, hotelId, null, null, null, null, null, null, 0, 10);
        if (rooms != null) {
            System.out.println("Доступ к списку комнат работает");
        }

        System.out.println("\n4.2 Создание комнаты");

        roomId = createTestRoom(adminToken, hotelId);
        if (roomId != null) {
            System.out.println("ADMIN создал комнату: " + roomId);
        }

        if (managerToken != null) {
            UUID managerRoomId = createTestRoom(managerToken, hotelId);
            if (managerRoomId != null) {
                System.out.println("MANAGER создал комнату в отеле");
                deleteRoom(adminToken, hotelId, managerRoomId);
            }
        }

        System.out.println("\n4.3 Получение комнаты по ID");

        if (roomId != null) {
            var room = getRoomById(customerToken, hotelId, roomId);
            if (room != null) {
                System.out.println("Комната получена по ID: type = " + room.get("roomType"));
            }
        }

        System.out.println("\n4.4 Поиск доступных комнат");

        if (roomId != null) {
            LocalDate checkIn = LocalDate.now().plusDays(1);
            LocalDate checkOut = LocalDate.now().plusDays(3);
            var availableRooms = getAvailableRooms(customerToken, hotelId, checkIn, checkOut);
            if (availableRooms != null) {
                System.out.println("Найдено доступных комнат: " + availableRooms.size());
            }
        }

        System.out.println("\n4.5 Обновление комнаты");

        if (roomId != null && adminToken != null) {
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("roomType", "SUITE");
            updateData.put("pricePerNight", 250.00);
            updateData.put("capacity", 4);
            updateData.put("description", "Обновленный люкс");

            var updatedRoom = updateRoom(adminToken, hotelId, roomId, updateData);
            if (updatedRoom != null) {
                System.out.println("ADMIN обновил комнату");
            }
        }
    }

    private static void testBookingManagement() {
        System.out.println("5. Тестирование брони");

        if (roomId == null || customerUserId == null) {
            System.out.println(" Нет комнаты или пользователя для тестирования бронирований");
            return;
        }

        System.out.println("\n5.1 Создание бронирования");

        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = LocalDate.now().plusDays(5);

        bookingId = createTestBooking(customerToken, customerUserId, roomId, checkIn, checkOut);
        if (bookingId != null) {
            System.out.println("CUSTOMER создал бронирование: " + bookingId);
        }

        System.out.println("\n5.2 Получение списка бронирований");

        var bookings = getBookings(customerToken, null, null, null, 0, 10);
        if (bookings != null) {
            System.out.println("CUSTOMER получил свои бронирования: " + bookings.size());
        }

        System.out.println("\n5.3 Получение бронирования по ID");

        if (bookingId != null) {
            var booking = getBookingById(customerToken, bookingId);
            if (booking != null) {
                System.out.println("Бронирование получено по ID: " + booking.get("bookingId"));
                System.out.println("  Статус: " + booking.get("status"));
            }
        }

        System.out.println("\n5.4 Обновление статуса бронирования");

        if (bookingId != null) {
            boolean confirmed = updateBookingStatus(managerToken, bookingId, "CONFIRMED");
            if (confirmed) {
                System.out.println("MANAGER подтвердил бронирование");
            }

            var booking = getBookingById(customerToken, bookingId);
            if (booking != null && "CONFIRMED".equals(booking.get("status"))) {
                System.out.println("Статус обновлен на CONFIRMED");
            }

            updateBookingStatus(adminToken, bookingId, "CANCELLED");
            System.out.println("Статус возвращен к CANCELLED");
        }

        System.out.println("\n5.5 Фильтрация бронирований");

        var cancelledBookings = getBookings(customerToken, null, null, "CANCELLED", 0, 10);
        if (cancelledBookings != null) {
            System.out.println("Фильтрация по статусу CANCELLED: " + cancelledBookings.size());
        }
    }

    private static void testCustomerFunctionality() {
        System.out.println("6. Тестирование клиента");

        System.out.println("\n6.1 Получение программы лояльности");

        if (customerToken != null && customerUserId != null) {
            var loyalty = getLoyaltyProgram(customerToken);
            if (loyalty != null) {
                System.out.println("Получена программа лояльности");
                System.out.println("  Баланс баллов: " + loyalty.get("pointsBalance"));
                System.out.println("  Уровень: " + loyalty.get("tier"));
            }
        }

        System.out.println("\n6.2 Создание запроса на отмену");

        if (bookingId != null && customerToken != null) {
            LocalDate checkIn = LocalDate.now().plusDays(10);
            LocalDate checkOut = LocalDate.now().plusDays(12);
            UUID testBookingId = createTestBooking(customerToken, customerUserId, roomId, checkIn, checkOut);

            if (testBookingId != null) {
                updateBookingStatus(managerToken, testBookingId, "CONFIRMED");

                cancelRequestId = createCancelRequest(customerToken, testBookingId, "Хочу отменить бронирование");
                if (cancelRequestId != null) {
                    System.out.println("Создан запрос на отмену: " + cancelRequestId);

                    var requests = getMyCancelRequests(customerToken, 0, 10);
                    if (requests != null && !requests.isEmpty()) {
                        System.out.println("Получены свои запросы на отмену: " + requests.size());
                    }
                }
            }
        }
    }

    private static void testManagerFunctionality() {
        System.out.println("7. Тестирование менеджеров");

        System.out.println("\n7.1 Получение всех запросов на отмену");

        if (managerToken != null) {
            var allRequests = getAllCancelRequests(managerToken, 0, 10);
            if (allRequests != null) {
                System.out.println("Получены все запросы на отмену: " + allRequests.size());
            }
        }

        System.out.println("\n7.2 Получение PENDING запросов");

        if (managerToken != null) {
            var pendingRequests = getPendingCancelRequests(managerToken, 0, 10);
            if (pendingRequests != null) {
                System.out.println("Получены PENDING запросы: " + pendingRequests.size());

                if (!pendingRequests.isEmpty() && cancelRequestId != null) {
                    System.out.println("\n7.3 Обработка запроса на отмену");

                    boolean approved = processCancelRequest(managerToken, cancelRequestId, "APPROVED", "Одобрено");
                    if (approved) {
                        System.out.println("Запрос на отмену одобрен");

                        if (bookingId != null) {
                            var booking = getBookingById(customerToken, bookingId);
                            if (booking != null) {
                                System.out.println("  Статус бронирования: " + booking.get("status"));
                            }
                        }
                    }
                }
            }
        }

        System.out.println("\n7.4 Получение аудит-логов");

        if (managerToken != null) {
            var auditLogs = getAuditLogs(managerToken, 0, 10);
            if (auditLogs != null) {
                System.out.println("Получены аудит-логи: " + auditLogs.size());
            }
        }
    }

    private static void testIntegrationScenarios() {
        System.out.println("8. Интеграционные сценарии");

        System.out.println("\n8.1 Полный сценарий бронирования");

        RegistrationResult newUser = createTestUser();
        if (newUser != null) {
            System.out.println("Новый пользователь зарегистрирован: " + newUser.getUsername());
            var hotels = getHotelsWithFilters(newUser.getAccessToken(), null, null, null, null, 0, 10);
            if (hotels != null && !hotels.isEmpty()) {
                System.out.println("Найден отель");
                UUID newHotelId = createTestHotel(adminToken, "Интеграционный Отель_" + System.currentTimeMillis());
                if (newHotelId != null) {
                    UUID newRoomId = createTestRoom(adminToken, newHotelId);
                    if (newRoomId != null) {
                        LocalDate checkIn = LocalDate.now().plusDays(1);
                        LocalDate checkOut = LocalDate.now().plusDays(3);
                        UUID newBookingId = createTestBooking(
                                newUser.getAccessToken(),
                                newUser.getUserId(),
                                newRoomId,
                                checkIn,
                                checkOut
                        );

                        if (newBookingId != null) {
                            System.out.println("Бронирование создано: " + newBookingId);

                            updateBookingStatus(managerToken, newBookingId, "CONFIRMED");
                            System.out.println("Бронирование подтверждено менеджером");

                            var booking = getBookingById(newUser.getAccessToken(), newBookingId);
                            if (booking != null && "CONFIRMED".equals(booking.get("status"))) {
                                System.out.println("Статус бронирования: CONFIRMED");
                            }

                            updateBookingStatus(adminToken, newBookingId, "CANCELLED");
                            deleteBooking(adminToken, newBookingId);
                            deleteRoom(adminToken, newHotelId, newRoomId);
                            deleteHotel(adminToken, newHotelId);
                        }
                    }
                }
            }
            softDeleteUser(adminToken, newUser.getUserId());
        }

        System.out.println("\n8.2 Сценарий отмены бронирования");

        RegistrationResult cancelUser = createTestUser();
        if (cancelUser != null) {
            UUID cancelHotelId = createTestHotel(adminToken, "Отель для отмены_" + System.currentTimeMillis());
            if (cancelHotelId != null) {
                UUID cancelRoomId = createTestRoom(adminToken, cancelHotelId);
                if (cancelRoomId != null) {
                    LocalDate checkIn = LocalDate.now().plusDays(5);
                    LocalDate checkOut = LocalDate.now().plusDays(7);
                    UUID cancelBookingId = createTestBooking(
                            cancelUser.getAccessToken(),
                            cancelUser.getUserId(),
                            cancelRoomId,
                            checkIn,
                            checkOut
                    );

                    if (cancelBookingId != null) {
                        updateBookingStatus(managerToken, cancelBookingId, "CONFIRMED");

                        UUID cancelReqId = createCancelRequest(
                                cancelUser.getAccessToken(),
                                cancelBookingId,
                                "Тестовая отмена"
                        );

                        if (cancelReqId != null) {
                            System.out.println("Создан запрос на отмену");

                            processCancelRequest(managerToken, cancelReqId, "APPROVED", "Одобрено");
                            System.out.println("Запрос на отмену одобрен");

                            var booking = getBookingById(cancelUser.getAccessToken(), cancelBookingId);
                            if (booking != null && "CANCELLED".equals(booking.get("status"))) {
                                System.out.println("Бронирование отменено");
                            }
                        }

                        deleteBooking(adminToken, cancelBookingId);
                    }
                    deleteRoom(adminToken, cancelHotelId, cancelRoomId);
                    deleteHotel(adminToken, cancelHotelId);
                }
            }
            softDeleteUser(adminToken, cancelUser.getUserId());
        }
    }

    private static void testSecurity() {
        System.out.println(" 9. тесты безопасности");
        
        System.out.println("\n9.1 Проверка доступа к ADMIN эндпоинтам");

        if (customerToken != null) {
            var users = getAllUsers(customerToken);
            if (users == null) {
                System.out.println("CUSTOMER не имеет доступа к /api/admin/users");
            }
            
            boolean roleChanged = changeUserRole(customerToken, UUID.randomUUID(), "MANAGER");
            if (!roleChanged) {
                System.out.println("CUSTOMER не имеет доступа к изменению роли");
            }
            
            boolean deleted = softDeleteUser(customerToken, UUID.randomUUID());
            if (!deleted) {
                System.out.println("CUSTOMER не имеет доступа к удалению пользователя");
            }
        }
        
        System.out.println("\n9.2 Проверка доступа к MANAGER эндпоинтам");

        if (customerToken != null) {
            var requests = getAllCancelRequests(customerToken, 0, 10);
            if (requests == null) {
                System.out.println("CUSTOMER не имеет доступа к /api/manager/cancel-requests");
            }

            var auditLogs = getAuditLogs(customerToken, 0, 10);
            if (auditLogs == null) {
                System.out.println("CUSTOMER не имеет доступа к /api/manager/audit");
            }
        }
    }

    private static void cleanup() {
        System.out.println("Очистка тестовых данных");
        
        cleanupTestData(adminToken, customerToken, hotelId, roomId, bookingId, customerUserId);
        
        if (managerUserId != null && adminToken != null) {
            softDeleteUser(adminToken, managerUserId);
            System.out.println("Менеджер удален");
        }

        if (secondCustomerUserId != null && adminToken != null) {
            softDeleteUser(adminToken, secondCustomerUserId);
            System.out.println("Второй клиент удален");
        }

        System.out.println("\nОчистка завершена!");
    }

}