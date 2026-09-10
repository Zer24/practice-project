package org.example.test;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ApiTestUtils {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String AUTH_URL = BASE_URL + "/api/auth/login";
    private static final String REGISTER_URL = BASE_URL + "/api/auth/register";
    private static final String BOOKINGS_URL = BASE_URL + "/api/bookings";
    private static final String HOTELS_URL = BASE_URL + "/api/hotels";
    private static final String ADMIN_USERS_URL = BASE_URL + "/api/admin/users";

    private static final RestTemplate restTemplate = new RestTemplate();
    public static AuthResult login(String login, String password) {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("login", login);
        loginRequest.put("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(loginRequest, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    AUTH_URL,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                String accessToken = (String) responseBody.get("token");
                String refreshToken = (String) responseBody.get("refreshToken");

                System.out.println("Успешная авторизация пользователя: " + login);
                return new AuthResult(accessToken, refreshToken);
            } else {
                System.out.println("Ошибка авторизации: " + response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Исключение при авторизации: " + e.getMessage());
            return null;
        }
    }
    public static String loginAdmin() {
        AuthResult result = login("admin", "1234");
        return result != null ? result.getAccessToken() : null;
    }
    public static RegistrationResult registerUser(Map<String, Object> userData) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(userData, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    REGISTER_URL,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED) {
                Map<String, Object> responseBody = response.getBody();
                UUID userId = UUID.fromString((String) responseBody.get("userId"));
                String username = (String) responseBody.get("username");
                String email = (String) responseBody.get("email");
                String role = (String) responseBody.get("role");

                System.out.println("Пользователь зарегистрирован успешно: " + username);

                // Автоматически авторизуемся
                AuthResult authResult = login(username, (String) userData.get("password"));

                return new RegistrationResult(userId, username, email, role,
                        authResult != null ? authResult.getAccessToken() : null,
                        authResult != null ? authResult.getRefreshToken() : null);
            } else {
                System.out.println("Ошибка регистрации: " + response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Исключение при регистрации: " + e.getMessage());
            return null;
        }
    }
    public static RegistrationResult createTestUser() {
        String username = "testuser_" + System.currentTimeMillis();
        String email = "testuser_" + System.currentTimeMillis() + "@example.com";
        String password = "Test1234!";

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("email", email);
        userData.put("password", password);
        userData.put("phone", "+375291234567");
        userData.put("firstName", "Тест");
        userData.put("lastName", "Пользователь");

        return registerUser(userData);
    }
    public static List<Map<String, Object>> getAllUsers(String adminToken) {
        return getUsersWithFilter(adminToken, null, null);
    }
    public static List<Map<String, Object>> getUsersByUsername(String adminToken, String username) {
        return getUsersWithFilter(adminToken, username, null);
    }
    public static List<Map<String, Object>> getUsersByEmail(String adminToken, String email) {
        return getUsersWithFilter(adminToken, null, email);
    }

    private static List<Map<String, Object>> getUsersWithFilter(String adminToken, String username, String email) {
        if (adminToken == null) {
            System.out.println("Нет токена администратора.");
            return null;
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(ADMIN_USERS_URL)
                .queryParam("page", 0)
                .queryParam("size", 100);

        if (username != null) {
            builder.queryParam("username", username);
        }
        if (email != null) {
            builder.queryParam("email", email);
        }

        String url = builder.toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                return (List<Map<String, Object>>) responseBody.get("content");
            } else {
                System.out.println("Ошибка получения пользователей: " + response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Исключение при получении пользователей: " + e.getMessage());
            return null;
        }
    }
    public static boolean changeUserRole(String adminToken, UUID userId, String newRole) {
        if (adminToken == null || userId == null) {
            System.out.println("Недостаточно данных для изменения роли.");
            return false;
        }

        String url = ADMIN_USERS_URL + "/" + userId + "/role?newRole=" + newRole;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("Роль пользователя изменена на " + newRole);
                return true;
            } else {
                System.out.println("Ошибка изменения роли: " + response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Исключение при изменении роли: " + e.getMessage());
            return false;
        }
    }
    public static boolean softDeleteUser(String adminToken, UUID userId) {
        return deleteUserInternal(adminToken, userId, false);
    }

    private static boolean deleteUserInternal(String adminToken, UUID userId, boolean hard) {
        if (adminToken == null || userId == null) {
            System.out.println("Недостаточно данных для удаления пользователя.");
            return false;
        }

        String url = ADMIN_USERS_URL + "/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    requestEntity,
                    Void.class
            );

            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                System.out.println("Пользователь удален");
                return true;
            } else {
                System.out.println("Ошибка удаления: " + response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Исключение при удалении: " + e.getMessage());
            return false;
        }
    }
    public static boolean restoreUser(String adminToken, UUID userId) {
        if (adminToken == null || userId == null) {
            System.out.println("Недостаточно данных для восстановления пользователя.");
            return false;
        }

        String url = ADMIN_USERS_URL + "/" + userId + "/restore";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    requestEntity,
                    Void.class
            );

            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                System.out.println("Пользователь восстановлен");
                return true;
            } else {
                System.out.println("Ошибка восстановления: " + response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Исключение при восстановлении: " + e.getMessage());
            return false;
        }
    }
    public static UUID createHotel(String adminToken, Map<String, Object> hotelData) {
        if (adminToken == null) {
            System.out.println("Нет токена администратора.");
            return null;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(hotelData, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    HOTELS_URL,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED) {
                Map<String, Object> responseBody = response.getBody();
                UUID hotelId = UUID.fromString((String) responseBody.get("hotelId"));
                System.out.println("Отель создан: " + responseBody.get("name"));
                return hotelId;
            } else {
                System.out.println("Ошибка создания отеля: " + response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Исключение при создании отеля: " + e.getMessage());
            return null;
        }
    }
    public static UUID createTestHotel(String adminToken, String name) {
        Map<String, Object> hotelData = new HashMap<>();
        hotelData.put("name", name);
        hotelData.put("city", "Минск");
        hotelData.put("country", "Беларусь");
        hotelData.put("address", "ул. Тестовая, 1");
        hotelData.put("rating", 4.5);

        return createHotel(adminToken, hotelData);
    }
    public static boolean deleteHotel(String adminToken, UUID hotelId) {
        if (adminToken == null || hotelId == null) {
            return false;
        }

        String url = HOTELS_URL + "/" + hotelId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    requestEntity,
                    Void.class
            );

            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                System.out.println("Отель удален");
                return true;
            } else {
                System.out.println("Ошибка удаления отеля: " + response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Исключение при удалении отеля: " + e.getMessage());
            return false;
        }
    }
    public static UUID createRoom(String adminToken, UUID hotelId, Map<String, Object> roomData) {
        if (adminToken == null || hotelId == null) {
            System.out.println("Недостаточно данных для создания комнаты.");
            return null;
        }

        String url = HOTELS_URL + "/" + hotelId + "/rooms";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(roomData, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED) {
                Map<String, Object> responseBody = response.getBody();
                UUID roomId = UUID.fromString((String) responseBody.get("roomId"));
                System.out.println("Комната создана: " + responseBody.get("roomType"));
                return roomId;
            } else {
                System.out.println("Ошибка создания комнаты: " + response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Исключение при создании комнаты: " + e.getMessage());
            return null;
        }
    }
    public static UUID createTestRoom(String adminToken, UUID hotelId) {
        Map<String, Object> roomData = new HashMap<>();
        roomData.put("hotelId", hotelId.toString());
        roomData.put("roomType", "DELUXE");
        roomData.put("pricePerNight", 150.00);
        roomData.put("capacity", 2);
        roomData.put("description", "Люкс с видом на море");
        roomData.put("amenities", List.of("Wi-Fi", "Кондиционер", "Мини-бар", "Телевизор"));
        roomData.put("area", 35.5);

        return createRoom(adminToken, hotelId, roomData);
    }
    public static boolean deleteRoom(String adminToken, UUID hotelId, UUID roomId) {
        if (adminToken == null || hotelId == null || roomId == null) {
            return false;
        }

        String url = HOTELS_URL + "/" + hotelId + "/rooms/" + roomId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    requestEntity,
                    Void.class
            );

            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                System.out.println("Комната удалена");
                return true;
            } else {
                System.out.println("Ошибка удаления комнаты: " + response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Исключение при удалении комнаты: " + e.getMessage());
            return false;
        }
    }
    public static UUID createBooking(String userToken, Map<String, Object> bookingData) {
        if (userToken == null) {
            System.out.println("Нет токена пользователя.");
            return null;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(userToken);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(bookingData, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    BOOKINGS_URL,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED) {
                Map<String, Object> responseBody = response.getBody();
                UUID bookingId = UUID.fromString((String) responseBody.get("bookingId"));
                System.out.println("Бронирование создано: " + bookingId);
                return bookingId;
            } else {
                System.out.println("Ошибка создания бронирования: " + response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Исключение при создании бронирования: " + e.getMessage());
            return null;
        }
    }
    public static UUID createTestBooking(String userToken, UUID userId, UUID roomId,
                                         LocalDate checkInDate, LocalDate checkOutDate) {
        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("userId", userId.toString());
        bookingData.put("roomId", roomId.toString());
        bookingData.put("checkInDate", checkInDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        bookingData.put("checkOutDate", checkOutDate.format(DateTimeFormatter.ISO_LOCAL_DATE));

        return createBooking(userToken, bookingData);
    }
    public static Map<String, Object> getBookingById(String userToken, UUID bookingId) {
        if (userToken == null || bookingId == null) {
            return null;
        }

        String url = BOOKINGS_URL + "/" + bookingId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                System.out.println("Ошибка получения бронирования: " + response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Исключение при получении бронирования: " + e.getMessage());
            return null;
        }
    }
    public static List<Map<String, Object>> getBookings(String userToken, LocalDate fromDate,
                                                        LocalDate toDate, String status,
                                                        int page, int size) {
        if (userToken == null) {
            return null;
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(BOOKINGS_URL)
                .queryParam("page", page)
                .queryParam("size", size);

        if (fromDate != null) {
            builder.queryParam("fromDate", fromDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (toDate != null) {
            builder.queryParam("toDate", toDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (status != null) {
            builder.queryParam("status", status);
        }

        String url = builder.toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                return (List<Map<String, Object>>) responseBody.get("content");
            } else {
                System.out.println("Ошибка получения бронирований: " + response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Исключение при получении бронирований: " + e.getMessage());
            return null;
        }
    }
    public static boolean updateBookingStatus(String userToken, UUID bookingId, String status) {
        if (userToken == null || bookingId == null) {
            return false;
        }

        String url = BOOKINGS_URL + "/" + bookingId;

        Map<String, Object> statusUpdate = new HashMap<>();
        statusUpdate.put("status", status);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(userToken);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(statusUpdate, headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    requestEntity,
                    Void.class
            );

            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                System.out.println("Статус бронирования обновлен на " + status);
                return true;
            } else {
                System.out.println("Ошибка обновления статуса: " + response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Исключение при обновлении статуса: " + e.getMessage());
            return false;
        }
    }
    public static boolean deleteBooking(String adminToken, UUID bookingId) {
        if (adminToken == null || bookingId == null) {
            return false;
        }

        String url = BOOKINGS_URL + "/" + bookingId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    requestEntity,
                    Void.class
            );

            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                System.out.println("Бронирование удалено");
                return true;
            } else {
                System.out.println("Ошибка удаления бронирования: " + response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Исключение при удалении бронирования: " + e.getMessage());
            return false;
        }
    }
    public static boolean sendInvalidBooking(String userToken, Map<String, Object> bookingData,
                                             HttpStatus expectedStatus) {
        if (userToken == null) {
            return false;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(userToken);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(bookingData, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    BOOKINGS_URL,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == expectedStatus) {
                System.out.println("  Получен ожидаемый статус: " + expectedStatus);
                return true;
            } else {
                System.out.println("   Ожидался " + expectedStatus + ", получен: " + response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            String message = e.getMessage();
            if (message.contains(String.valueOf(expectedStatus.value()))) {
                System.out.println("  Получен ожидаемый статус: " + expectedStatus);
                return true;
            } else {
                System.err.println("  Исключение: " + e.getMessage());
                return false;
            }
        }
    }
    public static void cleanupTestData(String adminToken, String userToken,
                                       UUID hotelId, UUID roomId, UUID bookingId, UUID userId) {
        System.out.println("\nОчистка тестовых данных...");

        if (bookingId != null && userToken != null) {
            updateBookingStatus(userToken, bookingId, "CANCELLED");
            deleteBooking(adminToken, bookingId);
        }

        if (roomId != null && hotelId != null && adminToken != null) {
            deleteRoom(adminToken, hotelId, roomId);
        }

        if (hotelId != null && adminToken != null) {
            deleteHotel(adminToken, hotelId);
        }

        if (userId != null && adminToken != null) {
            softDeleteUser(adminToken, userId);
        }

        System.out.println("Очистка завершена");
    }
    public static Map<String, Object> getHotel(String token, UUID hotelId) {
        if (token == null || hotelId == null) {
            System.out.println("Недостаточно данных для получения отеля.");
            return null;
        }

        String url = HOTELS_URL + "/" + hotelId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                System.out.println("Ошибка получения отеля: " + response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Исключение при получении отеля: " + e.getMessage());
            return null;
        }
    }

    public static List<Map<String, Object>> getHotelsWithFilters(String token, String city, String country,
                                                                 Double minRating, Double maxRating,
                                                                 int page, int size) {
        if (token == null) {
            System.out.println("Нет токена.");
            return null;
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(HOTELS_URL)
                .queryParam("page", page)
                .queryParam("size", size);

        if (city != null && !city.isEmpty()) {
            builder.queryParam("city", city);
        }
        if (country != null && !country.isEmpty()) {
            builder.queryParam("country", country);
        }
        if (minRating != null) {
            builder.queryParam("minRating", minRating);
        }
        if (maxRating != null) {
            builder.queryParam("maxRating", maxRating);
        }

        String url = builder.toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null) {
                    System.out.println("Общее количество отелей: " + responseBody.get("totalElements"));
                    System.out.println("На странице: " + responseBody.get("numberOfElements"));
                    return (List<Map<String, Object>>) responseBody.get("content");
                }
                return null;
            } else {
                System.out.println("Ошибка получения отелей: " + response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Исключение при получении отелей: " + e.getMessage());
            return null;
        }
    }
    public static Map<String, Object> updateHotel(String token, UUID hotelId, Map<String, Object> hotelData) {
        if (token == null || hotelId == null) {
            System.out.println("Недостаточно данных для обновления отеля.");
            return null;
        }

        String url = HOTELS_URL + "/" + hotelId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(hotelData, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("Отель обновлен");
                return response.getBody();
            } else {
                System.out.println("Ошибка обновления отеля: " + response.getStatusCode());
                if (response.getBody() != null) {
                    System.out.println("Сообщение: " + response.getBody());
                }
                return null;
            }
        } catch (Exception e) {
            System.err.println("Исключение при обновлении отеля: " + e.getMessage());
            return null;
        }
    }
    public static class AuthResult {
        private final String accessToken;
        private final String refreshToken;

        public AuthResult(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
    }

    public static class RegistrationResult {
        private final UUID userId;
        private final String username;
        private final String email;
        private final String role;
        private final String accessToken;
        private final String refreshToken;

        public RegistrationResult(UUID userId, String username, String email, String role,
                                  String accessToken, String refreshToken) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.role = role;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public UUID getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
    }

    public static Map<String, Object> getLoyaltyProgram(String token) {
        if (token == null) {
            System.out.println("Нет токена для получения программы лояльности");
            return null;
        }

        String url = BASE_URL + "/api/customer/loyalty/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static UUID createCancelRequest(String token, UUID bookingId, String reason) {
        if (token == null || bookingId == null) {
            return null;
        }

        String url = BASE_URL + "/api/customer/cancel-requests";

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("bookingId", bookingId.toString());
        requestData.put("reason", reason);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestData, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED) {
                Map<String, Object> body = response.getBody();
                return UUID.fromString((String) body.get("requestId"));
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static List<Map<String, Object>> getMyCancelRequests(String token, int page, int size) {
        if (token == null) {
            return null;
        }

        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/api/customer/cancel-requests")
                .queryParam("page", page)
                .queryParam("size", size)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> body = response.getBody();
                return (List<Map<String, Object>>) body.get("content");
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static List<Map<String, Object>> getAllCancelRequests(String token, int page, int size) {
        if (token == null) {
            return null;
        }

        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/api/manager/cancel-requests")
                .queryParam("page", page)
                .queryParam("size", size)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> body = response.getBody();
                return (List<Map<String, Object>>) body.get("content");
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static List<Map<String, Object>> getPendingCancelRequests(String token, int page, int size) {
        if (token == null) {
            return null;
        }

        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/api/manager/cancel-requests/pending")
                .queryParam("page", page)
                .queryParam("size", size)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> body = response.getBody();
                return (List<Map<String, Object>>) body.get("content");
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean processCancelRequest(String token, UUID requestId, String status, String comment) {
        if (token == null || requestId == null) {
            return false;
        }

        String url = BASE_URL + "/api/manager/cancel-requests/" + requestId;

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("status", status);
        requestData.put("comment", comment);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestData, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    requestEntity,
                    Map.class
            );

            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            return false;
        }
    }

    public static List<Map<String, Object>> getAuditLogs(String token, int page, int size) {
        if (token == null) {
            return null;
        }

        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/api/manager/audit")
                .queryParam("page", page)
                .queryParam("size", size)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> body = response.getBody();
                return (List<Map<String, Object>>) body.get("content");
            }
            return null;
        } catch (Exception e) {
            System.out.println("Error: "+e.getMessage());
            return null;
        }
    }

    public static List<Map<String, Object>> getRoomsByHotel(String token, UUID hotelId, String roomType,
                                                             Integer capacity, Double minPrice, Double maxPrice,
                                                             String amenities, String sort, int page, int size) {
        if (hotelId == null) {
            return null;
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/api/hotels/" + hotelId + "/rooms")
                .queryParam("page", page)
                .queryParam("size", size);

        if (roomType != null) builder.queryParam("roomType", roomType);
        if (capacity != null) builder.queryParam("capacity", capacity);
        if (minPrice != null) builder.queryParam("minPrice", minPrice);
        if (maxPrice != null) builder.queryParam("maxPrice", maxPrice);
        if (amenities != null) builder.queryParam("amenities", amenities);
        if (sort != null) builder.queryParam("sort", sort);

        String url = builder.toUriString();

        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.setBearerAuth(token);
        }

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> body = response.getBody();
                return (List<Map<String, Object>>) body.get("content");
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static Map<String, Object> getRoomById(String token, UUID hotelId, UUID roomId) {
        if (hotelId == null || roomId == null) {
            return null;
        }

        String url = BASE_URL + "/api/hotels/" + hotelId + "/rooms/" + roomId;

        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.setBearerAuth(token);
        }

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static List<Map<String, Object>> getAvailableRooms(String token, UUID hotelId,
                                                               LocalDate checkIn, LocalDate checkOut) {
        if (hotelId == null || checkIn == null || checkOut == null) {
            return null;
        }

        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/api/hotels/" + hotelId + "/rooms/available")
                .queryParam("checkIn", checkIn.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .queryParam("checkOut", checkOut.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.setBearerAuth(token);
        }

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    List.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static Map<String, Object> updateRoom(String token, UUID hotelId, UUID roomId,
                                                  Map<String, Object> updateData) {
        if (token == null || hotelId == null || roomId == null) {
            return null;
        }

        String url = BASE_URL + "/api/hotels/" + hotelId + "/rooms/" + roomId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(updateData, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static AuthResult refreshTokens(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            System.out.println("Refresh токен не может быть пустым");
            return null;
        }

        Map<String, String> request = new HashMap<>();
        request.put("refreshToken", refreshToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/api/auth/tokens",
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null) {
                    String newAccessToken = (String) responseBody.get("token");
                    String newRefreshToken = (String) responseBody.get("refreshToken");

                    System.out.println("Токены успешно обновлены");
                    return new AuthResult(newAccessToken, newRefreshToken);
                }
            } else {
                System.out.println("Ошибка обновления токенов: " + response.getStatusCode());
                if (response.getBody() != null) {
                    System.out.println("Сообщение: " + response.getBody());
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("Исключение при обновлении токенов: " + e.getMessage());
            return null;
        }
    }
    public static boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        String url = BASE_URL + "/api/hotels";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isRefreshTokenValid(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            return false;
        }

        AuthResult result = refreshTokens(refreshToken);
        return result != null;
    }
    public static boolean logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            System.out.println("Refresh токен не может быть пустым");
            return false;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(refreshToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    BASE_URL + "/api/auth/logout",
                    HttpMethod.POST,
                    requestEntity,
                    Void.class
            );

            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                System.out.println("Logout выполнен успешно");
                return true;
            } else {
                System.out.println("Ошибка при выполнении logout: " + response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Исключение при выполнении logout: " + e.getMessage());
            return false;
        }
    }
}