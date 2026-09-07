package org.example.test;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HotelApiTestDebug {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String AUTH_URL = BASE_URL + "/api/auth/login";
    private static final String HOTELS_URL = BASE_URL + "/api/hotels";

    private static RestTemplate restTemplate = new RestTemplate();
    private static String accessToken;
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

            // 4. Тестирование различных вариантов фильтрации
            System.out.println("\n=== 4. Тестирование фильтрации ===");
            testDifferentFilters();

        } catch (Exception e) {
            System.err.println("Ошибка при выполнении теста: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void login() {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("login", "admin");
        loginRequest.put("password", "1234");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                AUTH_URL,
                HttpMethod.POST,
                requestEntity,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            accessToken = (String) responseBody.get("token");
            System.out.println("✅ Успешная авторизация");
        } else {
            System.out.println("❌ Ошибка авторизации");
        }
    }

    private static void createHotel() {
        String createUrl = HOTELS_URL;

        Map<String, Object> hotelData = new HashMap<>();
        hotelData.put("name", "Тестовый Отель");
        hotelData.put("city", "Минск");
        hotelData.put("country", "Беларусь");
        hotelData.put("address", "ул. Тестовая, 1");
        hotelData.put("rating", 4.5);

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
            } else {
                System.out.println("❌ Ошибка создания отеля: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("❌ Исключение при создании отеля: " + e.getMessage());
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
                System.out.println("Рейтинг: " + responseBody.get("rating"));
            }
        } catch (Exception e) {
            System.err.println("❌ Исключение при получении отеля: " + e.getMessage());
        }
    }

    private static void testDifferentFilters() {
        System.out.println("4.1. Запрос без фильтров:");
        testFilter(null, null, null, null);

        System.out.println("\n4.2. Поиск по городу 'Минск':");
        testFilter("Минск", null, null, null);

        System.out.println("\n4.3. Поиск по городу 'Минск' и стране 'Беларусь':");
        testFilter("Минск", "Беларусь", null, null);

        System.out.println("\n4.4. Поиск по рейтингу от 4.0 до 5.0:");
        testFilter(null, null, 4.0, 5.0);

        System.out.println("\n4.5. Поиск по городу 'Минск' и рейтингу от 4.0 до 5.0:");
        testFilter("Минск", null, 4.0, 5.0);

        System.out.println("\n4.6. Поиск по частичному совпадению города (регистр):");
        testFilter("минск", null, null, null);

        System.out.println("\n4.7. Поиск по пустому городу:");
        testFilter("", null, null, null);
    }

    private static void testFilter(String city, String country, Double minRating, Double maxRating) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(HOTELS_URL);

        if (city != null) builder.queryParam("city", city);
        if (country != null) builder.queryParam("country", country);
        if (minRating != null) builder.queryParam("minRating", minRating);
        if (maxRating != null) builder.queryParam("maxRating", maxRating);
        builder.queryParam("page", 0);
        builder.queryParam("size", 10);

        String url = builder.toUriString();
        System.out.println("URL: " + url);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            System.out.println("Статус: " + response.getStatusCode());
            System.out.println("Ответ: " + response.getBody());

            if (response.getStatusCode() == HttpStatus.OK) {
                // Парсим ответ вручную, чтобы проверить содержимое
                String body = response.getBody();
                if (body != null && body.contains("totalElements")) {
                    System.out.println("✅ Запрос выполнен успешно");
                } else {
                    System.out.println("⚠️ Ответ не содержит данных");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка запроса: " + e.getMessage());
        }
        System.out.println("---");
    }
}