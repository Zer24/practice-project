package org.example.test;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HotelApiTest {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String AUTH_URL = BASE_URL + "/api/auth/login";
    private static final String HOTELS_URL = BASE_URL + "/api/hotels";

    private static RestTemplate restTemplate = new RestTemplate();
    private static String accessToken;
    private static String refreshToken;
    private static UUID createdHotelId;

    public static void main(String[] args) {
        try {
            // 1. Авторизация
            System.out.println("=== 1. Авторизация ===");
            login();

            // 2. Создание отеля
            System.out.println("\n=== 2. Создание отеля ===");
            createHotel();

            // 3. Получение отеля по ID
            System.out.println("\n=== 3. Получение отеля по ID ===");
            getHotelById();

            // 4. Получение всех отелей с фильтрацией
            System.out.println("\n=== 4. Получение всех отелей с фильтрацией ===");
            getAllHotelsWithFilters();

            // 5. Обновление отеля
            System.out.println("\n=== 5. Обновление отеля ===");
            updateHotel();

            // 6. Удаление отеля
            System.out.println("\n=== 6. Удаление отеля ===");
            deleteHotel();

        } catch (Exception e) {
            System.err.println("Ошибка при выполнении теста: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void login() {
        String loginUrl = AUTH_URL;

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("login", "admin");
        loginRequest.put("password", "1234");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                loginUrl,
                HttpMethod.POST,
                requestEntity,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            accessToken = (String) responseBody.get("token");
            refreshToken = (String) responseBody.get("refreshToken");

            System.out.println("✅ Успешная авторизация");
            System.out.println("Access Token: " + accessToken.substring(0, 50) + "...");
            System.out.println("Refresh Token: " + refreshToken.substring(0, 50) + "...");
        } else {
            System.out.println("❌ Ошибка авторизации: " + response.getStatusCode());
        }
    }

    private static void createHotel() {
        if (accessToken == null) {
            System.out.println("❌ Нет access токена. Сначала выполните авторизацию.");
            return;
        }

        String createUrl = HOTELS_URL;

        Map<String, Object> hotelData = new HashMap<>();
        hotelData.put("name", "Тестовый Отель");
        hotelData.put("city", "Минск");
        hotelData.put("country", "Беларусь");
        hotelData.put("address", "ул. Тестовая, 1");
        hotelData.put("rating", 4.5);
        // managerId не указываем, он будет взят из текущего пользователя

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(hotelData, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    createUrl,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED) {
                Map<String, Object> responseBody = response.getBody();
                createdHotelId = UUID.fromString((String) responseBody.get("hotelId"));
                System.out.println("✅ Отель создан успешно!");
                System.out.println("ID отеля: " + createdHotelId);
                System.out.println("Название: " + responseBody.get("name"));
                System.out.println("Город: " + responseBody.get("city"));
                System.out.println("Страна: " + responseBody.get("country"));
                System.out.println("Адрес: " + responseBody.get("address"));
                System.out.println("Рейтинг: " + responseBody.get("rating"));
            } else {
                System.out.println("❌ Ошибка создания отеля: " + response.getStatusCode());
                if (response.getBody() != null) {
                    System.out.println("Сообщение: " + response.getBody());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Исключение при создании отеля: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void getHotelById() {
        if (createdHotelId == null) {
            System.out.println("❌ Нет ID созданного отеля.");
            return;
        }

        String getUrl = HOTELS_URL + "/" + createdHotelId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    getUrl,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                System.out.println("✅ Отель получен успешно!");
                System.out.println("ID: " + responseBody.get("hotelId"));
                System.out.println("Название: " + responseBody.get("name"));
                System.out.println("Город: " + responseBody.get("city"));
                System.out.println("Страна: " + responseBody.get("country"));
                System.out.println("Адрес: " + responseBody.get("address"));
                System.out.println("Рейтинг: " + responseBody.get("rating"));
            } else {
                System.out.println("❌ Ошибка получения отеля: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("❌ Исключение при получении отеля: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void getAllHotelsWithFilters() {
        String url = UriComponentsBuilder.fromHttpUrl(HOTELS_URL)
                .queryParam("city", "Минск")
                .queryParam("country", "Беларусь")
                .queryParam("minRating", 4.0)
                .queryParam("maxRating", 5.0)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

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
                System.out.println("✅ Отели с фильтрацией получены успешно!");

                if (responseBody != null) {
                    System.out.println("Общее количество: " + responseBody.get("totalElements"));
                    System.out.println("Количество на странице: " + responseBody.get("numberOfElements"));
                    System.out.println("Номер страницы: " + responseBody.get("number"));

                    var content = (java.util.ArrayList) responseBody.get("content");
                    if (content != null && !content.isEmpty()) {
                        System.out.println("Найдено отелей: " + content.size());
                        content.forEach(item -> {
                            Map hotel = (Map) item;
                            System.out.println("  - " + hotel.get("name") + " (" + hotel.get("city") + ", " + hotel.get("country") + ")");
                        });
                    }
                }
            } else {
                System.out.println("❌ Ошибка получения отелей: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("❌ Исключение при получении отелей: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void updateHotel() {
        if (createdHotelId == null) {
            System.out.println("❌ Нет ID созданного отеля.");
            return;
        }

        String updateUrl = HOTELS_URL + "/" + createdHotelId;

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("name", "Обновленный Тестовый Отель");
        updateData.put("city", "Гродно");
        updateData.put("country", "Беларусь");
        updateData.put("address", "ул. Обновленная, 10");
        updateData.put("rating", 4.8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(updateData, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    updateUrl,
                    HttpMethod.PUT,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                System.out.println("✅ Отель обновлен успешно!");
                System.out.println("ID: " + responseBody.get("hotelId"));
                System.out.println("Новое название: " + responseBody.get("name"));
                System.out.println("Новый город: " + responseBody.get("city"));
                System.out.println("Новый адрес: " + responseBody.get("address"));
                System.out.println("Новый рейтинг: " + responseBody.get("rating"));
            } else {
                System.out.println("❌ Ошибка обновления отеля: " + response.getStatusCode());
                if (response.getBody() != null) {
                    System.out.println("Сообщение: " + response.getBody());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Исключение при обновлении отеля: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void deleteHotel() {
        if (createdHotelId == null) {
            System.out.println("❌ Нет ID созданного отеля.");
            return;
        }

        String deleteUrl = HOTELS_URL + "/" + createdHotelId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    deleteUrl,
                    HttpMethod.DELETE,
                    requestEntity,
                    Void.class
            );

            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                System.out.println("✅ Отель удален успешно!");
            } else {
                System.out.println("❌ Ошибка удаления отеля: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("❌ Исключение при удалении отеля: " + e.getMessage());
            e.printStackTrace();
        }
    }
}