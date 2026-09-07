package org.example.test;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UserApiTest {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String AUTH_URL = BASE_URL + "/api/auth/login";
    private static final String REGISTER_URL = BASE_URL + "/api/auth/register";
    private static final String ADMIN_USERS_URL = BASE_URL + "/api/admin/users";

    private static RestTemplate restTemplate = new RestTemplate();
    private static String adminAccessToken;
    private static String adminRefreshToken;
    private static String userAccessToken;
    private static String userRefreshToken;
    private static UUID createdUserId;
    private static UUID createdUserIdForDeletion;
    private static String createdUsername;
    private static String createdUserEmail;

    public static void main(String[] args) {
        try {
            // 1. Авторизация администратора
            System.out.println("=== 1. Авторизация администратора ===");
            loginAdmin();

            // 2. Регистрация нового пользователя
            System.out.println("\n=== 2. Регистрация нового пользователя ===");
            registerUser();

            // 3. Получение всех пользователей
            System.out.println("\n=== 3. Получение всех пользователей ===");
            getAllUsers();

            // 4. Изменение роли пользователя
            System.out.println("\n=== 4. Изменение роли пользователя ===");
            changeUserRole();

            // 5. Мягкое удаление пользователя
            System.out.println("\n=== 5. Мягкое удаление пользователя ===");
            softDeleteUser();

            // 6. Восстановление пользователя
            System.out.println("\n=== 6. Восстановление пользователя ===");
            restoreUser();

            // 7. Жесткое удаление пользователя
            System.out.println("\n=== 7. Жесткое удаление пользователя ===");
            hardDeleteUser();

            // 8. Проверка на дублирование email
            System.out.println("\n=== 8. Проверка на дублирование email ===");
            testDuplicateEmail();

            // 9. Проверка регистрации с некорректными данными
            System.out.println("\n=== 9. Проверка регистрации с некорректными данными ===");
            testInvalidRegistration();

        } catch (Exception e) {
            System.err.println("Ошибка при выполнении теста: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== 1. АВТОРИЗАЦИЯ АДМИНИСТРАТОРА ====================

    private static void loginAdmin() {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("login", "admin");
        loginRequest.put("password", "1234");

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
                adminAccessToken = (String) responseBody.get("token");
                adminRefreshToken = (String) responseBody.get("refreshToken");

                System.out.println("✅ Успешная авторизация администратора");
                System.out.println("Access Token: " + adminAccessToken.substring(0, 50) + "...");
            } else {
                System.out.println("❌ Ошибка авторизации: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("❌ Исключение при авторизации: " + e.getMessage());
        }
    }

    // ==================== 2. РЕГИСТРАЦИЯ НОВОГО ПОЛЬЗОВАТЕЛЯ ====================

    private static void registerUser() {
        createdUsername = "testuser_" + System.currentTimeMillis();
        createdUserEmail = "testuser_" + System.currentTimeMillis() + "@example.com";

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", createdUsername);
        userData.put("email", createdUserEmail);
        userData.put("password", "Test1234!");
        userData.put("phone", "+375291234567");
        userData.put("firstName", "Тест");
        userData.put("lastName", "Пользователь");

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
                createdUserId = UUID.fromString((String) responseBody.get("userId"));

                System.out.println("✅ Пользователь зарегистрирован успешно!");
                System.out.println("ID пользователя: " + createdUserId);
                System.out.println("Имя пользователя: " + responseBody.get("username"));
                System.out.println("Email: " + responseBody.get("email"));
                System.out.println("Роль: " + responseBody.get("role"));

                loginAsUser(createdUsername, "Test1234!");
            } else {
                System.out.println("❌ Ошибка регистрации: " + response.getStatusCode());
                if (response.getBody() != null) {
                    System.out.println("Сообщение: " + response.getBody());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Исключение при регистрации: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== АВТОРИЗАЦИЯ ОБЫЧНОГО ПОЛЬЗОВАТЕЛЯ ====================

    private static void loginAsUser(String username, String password) {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("login", username);
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
                userAccessToken = (String) responseBody.get("token");
                userRefreshToken = (String) responseBody.get("refreshToken");

                System.out.println("✅ Успешная авторизация пользователя");
            } else {
                System.out.println("❌ Ошибка авторизации пользователя: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("❌ Исключение при авторизации пользователя: " + e.getMessage());
        }
    }

    // ==================== 3. ПОЛУЧЕНИЕ ВСЕХ ПОЛЬЗОВАТЕЛЕЙ ====================

    private static void getAllUsers() {
        if (adminAccessToken == null) {
            System.out.println("❌ Нет токена администратора.");
            return;
        }

        // Пробуем получить всех пользователей без фильтра
        getAllUsersWithoutFilters();

        // Затем пробуем с фильтром по username
        String url = UriComponentsBuilder.fromHttpUrl(ADMIN_USERS_URL)
                .queryParam("username", createdUsername)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminAccessToken);

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
                System.out.println("✅ Фильтрация по username выполнена!");

                if (responseBody != null) {
                    // Используем Number для безопасного приведения
                    Number totalElements = (Number) responseBody.get("totalElements");
                    System.out.println("Найдено пользователей с username '" + createdUsername + "': " +
                            (totalElements != null ? totalElements.longValue() : 0));

                    var content = (List<Map<String, Object>>) responseBody.get("content");
                    if (content != null && !content.isEmpty()) {
                        content.forEach(user -> {
                            System.out.println("  - " + user.get("username") +
                                    " (" + user.get("email") + ") [" + user.get("role") + "]");
                        });
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка фильтрации: " + e.getMessage());
        }
    }

    private static void getAllUsersWithoutFilters() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminAccessToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    ADMIN_USERS_URL,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                System.out.println("✅ Список всех пользователей:");

                if (responseBody != null) {
                    // Используем Number для безопасного приведения
                    Number totalElements = (Number) responseBody.get("totalElements");
                    System.out.println("Всего пользователей: " +
                            (totalElements != null ? totalElements.longValue() : 0));

                    var content = (List<Map<String, Object>>) responseBody.get("content");
                    if (content != null && !content.isEmpty()) {
                        content.forEach(user -> {
                            System.out.println("  - " + user.get("username") +
                                    " (" + user.get("email") + ") [" + user.get("role") + "]");
                        });
                    } else {
                        System.out.println("⚠️ Список пользователей пуст!");
                        System.out.println("  Возможно, в БД нет активных пользователей или нужно проверить подключение.");
                    }
                }
            } else {
                System.out.println("❌ Ошибка получения пользователей: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("❌ Исключение при получении пользователей: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== 4. ИЗМЕНЕНИЕ РОЛИ ПОЛЬЗОВАТЕЛЯ ====================

    private static void changeUserRole() {
        if (createdUserId == null) {
            System.out.println("❌ Нет ID созданного пользователя.");
            return;
        }

        String changeRoleUrl = ADMIN_USERS_URL + "/" + createdUserId + "/role?newRole=ADMIN";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminAccessToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    changeRoleUrl,
                    HttpMethod.PUT,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                System.out.println("✅ Роль пользователя изменена успешно!");
                System.out.println("ID: " + responseBody.get("userId"));
                System.out.println("Новая роль: " + responseBody.get("role"));
            } else {
                System.out.println("❌ Ошибка изменения роли: " + response.getStatusCode());
                if (response.getBody() != null) {
                    System.out.println("Сообщение: " + response.getBody());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Исключение при изменении роли: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== 5. МЯГКОЕ УДАЛЕНИЕ ПОЛЬЗОВАТЕЛЯ ====================

    private static void softDeleteUser() {
        if (createdUserId == null) {
            System.out.println("❌ Нет ID созданного пользователя.");
            return;
        }

        String softDeleteUrl = ADMIN_USERS_URL + "/" + createdUserId + "/soft";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminAccessToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    softDeleteUrl,
                    HttpMethod.DELETE,
                    requestEntity,
                    Void.class
            );

            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                System.out.println("✅ Пользователь мягко удален успешно!");

                // Проверяем, что пользователь не отображается в списке активных
                String url = UriComponentsBuilder.fromHttpUrl(ADMIN_USERS_URL)
                        .queryParam("username", createdUsername)
                        .queryParam("page", 0)
                        .queryParam("size", 10)
                        .toUriString();

                ResponseEntity<Map> checkResponse = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        Map.class
                );

                if (checkResponse.getStatusCode() == HttpStatus.OK) {
                    Map<String, Object> responseBody = checkResponse.getBody();
                    // Используем Number для безопасного приведения
                    Number totalElements = (Number) responseBody.get("totalElements");
                    long count = totalElements != null ? totalElements.longValue() : 0;
                    System.out.println("После мягкого удаления: пользователь не отображается (totalElements=" + count + ")");
                }
            } else {
                System.out.println("❌ Ошибка мягкого удаления: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("❌ Исключение при мягком удалении: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== 6. ВОССТАНОВЛЕНИЕ ПОЛЬЗОВАТЕЛЯ ====================

    private static void restoreUser() {
        if (createdUserId == null) {
            System.out.println("❌ Нет ID пользователя для восстановления.");
            return;
        }

        String restoreUrl = ADMIN_USERS_URL + "/" + createdUserId + "/restore";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminAccessToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    restoreUrl,
                    HttpMethod.PUT,
                    requestEntity,
                    Void.class
            );

            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                System.out.println("✅ Пользователь восстановлен успешно!");

                // Проверяем, что пользователь снова отображается
                String url = UriComponentsBuilder.fromHttpUrl(ADMIN_USERS_URL)
                        .queryParam("username", createdUsername)
                        .queryParam("page", 0)
                        .queryParam("size", 10)
                        .toUriString();

                ResponseEntity<Map> checkResponse = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        Map.class
                );

                if (checkResponse.getStatusCode() == HttpStatus.OK) {
                    Map<String, Object> responseBody = checkResponse.getBody();
                    // Используем Number для безопасного приведения
                    Number totalElements = (Number) responseBody.get("totalElements");
                    long count = totalElements != null ? totalElements.longValue() : 0;
                    System.out.println("После восстановления: пользователь снова виден (totalElements=" + count + ")");
                }
            } else {
                System.out.println("❌ Ошибка восстановления: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("❌ Исключение при восстановлении: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== 7. ЖЕСТКОЕ УДАЛЕНИЕ ПОЛЬЗОВАТЕЛЯ ====================

    private static void hardDeleteUser() {
        if (createdUserId == null) {
            System.out.println("❌ Нет ID пользователя для жесткого удаления.");
            return;
        }

        String hardDeleteUrl = ADMIN_USERS_URL + "/" + createdUserId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminAccessToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    hardDeleteUrl,
                    HttpMethod.DELETE,
                    requestEntity,
                    Void.class
            );

            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                System.out.println("✅ Пользователь жестко удален успешно!");
            } else {
                System.out.println("❌ Ошибка жесткого удаления: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("❌ Исключение при жестком удалении: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== 8. ПРОВЕРКА НА ДУБЛИРОВАНИЕ EMAIL ====================

    private static void testDuplicateEmail() {
        String testUsername = "duplicate_test_" + System.currentTimeMillis();
        String testEmail = "duplicate_test_" + System.currentTimeMillis() + "@example.com";

        Map<String, Object> firstUser = new HashMap<>();
        firstUser.put("username", testUsername);
        firstUser.put("email", testEmail);
        firstUser.put("password", "Test1234!");
        firstUser.put("phone", "+375291234571");
        firstUser.put("firstName", "Дубликат");
        firstUser.put("lastName", "Первый");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            // Регистрируем первого пользователя
            ResponseEntity<Map> firstResponse = restTemplate.exchange(
                    REGISTER_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(firstUser, headers),
                    Map.class
            );

            if (firstResponse.getStatusCode() != HttpStatus.CREATED) {
                System.out.println("❌ Не удалось создать первого пользователя");
                return;
            }

            System.out.println("✅ Первый пользователь создан с email: " + testEmail);

            // Пытаемся зарегистрировать второго с тем же email
            Map<String, Object> secondUser = new HashMap<>();
            secondUser.put("username", "duplicate2_" + System.currentTimeMillis());
            secondUser.put("email", testEmail);
            secondUser.put("password", "Test1234!");
            secondUser.put("phone", "+375291234572");
            secondUser.put("firstName", "Дубликат");
            secondUser.put("lastName", "Второй");

            ResponseEntity<Map> secondResponse = restTemplate.exchange(
                    REGISTER_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(secondUser, headers),
                    Map.class
            );

            if (secondResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
                System.out.println("✅ Дублирование email обнаружено (400 BAD_REQUEST)!");
                if (secondResponse.getBody() != null) {
                    System.out.println("Сообщение: " + secondResponse.getBody().get("message"));
                }
            } else if (secondResponse.getStatusCode() == HttpStatus.CONFLICT) {
                System.out.println("✅ Дублирование email обнаружено (409 CONFLICT)!");
            } else {
                System.out.println("⚠️ Неожиданный код ответа при дублировании: " + secondResponse.getStatusCode());
            }

            // Удаляем первого пользователя
            UUID userId = UUID.fromString((String) firstResponse.getBody().get("userId"));
            String deleteUrl = ADMIN_USERS_URL + "/" + userId;
            HttpHeaders adminHeaders = new HttpHeaders();
            adminHeaders.setBearerAuth(adminAccessToken);
            restTemplate.exchange(deleteUrl, HttpMethod.DELETE, new HttpEntity<>(adminHeaders), Void.class);
            System.out.println("  Очистка: тестовый пользователь удален");

        } catch (Exception e) {
            System.err.println("❌ Исключение при проверке дублирования: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== 9. ПРОВЕРКА РЕГИСТРАЦИИ С НЕКОРРЕКТНЫМИ ДАННЫМИ ====================

    private static void testInvalidRegistration() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Тест 1: Короткий пароль
        System.out.println("  Тест 1: Пароль слишком короткий");
        Map<String, Object> shortPassword = new HashMap<>();
        shortPassword.put("username", "shortpass_" + System.currentTimeMillis());
        shortPassword.put("email", "shortpass_" + System.currentTimeMillis() + "@example.com");
        shortPassword.put("password", "123");
        shortPassword.put("phone", "+375291234573");
        shortPassword.put("firstName", "Короткий");
        shortPassword.put("lastName", "Пароль");

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    REGISTER_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(shortPassword, headers),
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
                System.out.println("    ✅ Короткий пароль отклонен: " + response.getStatusCode());
            } else {
                System.out.println("    ⚠️ Ожидался 400 BAD_REQUEST, получен: " + response.getStatusCode());
            }
        } catch (Exception e) {
            if (e.getMessage().contains("400")) {
                System.out.println("    ✅ Короткий пароль отклонен (400 BAD_REQUEST)");
            } else {
                System.err.println("    ❌ Исключение: " + e.getMessage());
            }
        }

        // Тест 2: Неверный формат email
        System.out.println("  Тест 2: Неверный формат email");
        Map<String, Object> invalidEmail = new HashMap<>();
        invalidEmail.put("username", "invalidemail_" + System.currentTimeMillis());
        invalidEmail.put("email", "notanemail");
        invalidEmail.put("password", "Test1234!");
        invalidEmail.put("phone", "+375291234574");
        invalidEmail.put("firstName", "Неверный");
        invalidEmail.put("lastName", "Email");

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    REGISTER_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(invalidEmail, headers),
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
                System.out.println("    ✅ Неверный email отклонен: " + response.getStatusCode());
            } else {
                System.out.println("    ⚠️ Ожидался 400 BAD_REQUEST, получен: " + response.getStatusCode());
            }
        } catch (Exception e) {
            if (e.getMessage().contains("400")) {
                System.out.println("    ✅ Неверный email отклонен (400 BAD_REQUEST)");
            } else {
                System.err.println("    ❌ Исключение: " + e.getMessage());
            }
        }

        // Тест 3: Отсутствие обязательных полей
        System.out.println("  Тест 3: Отсутствие обязательных полей");
        Map<String, Object> missingFields = new HashMap<>();
        missingFields.put("username", "missing_" + System.currentTimeMillis());
        missingFields.put("password", "Test1234!");
        // Нет email

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    REGISTER_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(missingFields, headers),
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
                System.out.println("    ✅ Отсутствие обязательных полей обнаружено: " + response.getStatusCode());
            } else {
                System.out.println("    ⚠️ Ожидался 400 BAD_REQUEST, получен: " + response.getStatusCode());
            }
        } catch (Exception e) {
            if (e.getMessage().contains("400")) {
                System.out.println("    ✅ Отсутствие обязательных полей обнаружено (400 BAD_REQUEST)");
            } else {
                System.err.println("    ❌ Исключение: " + e.getMessage());
            }
        }

        System.out.println("✅ Тестирование некорректной регистрации завершено");
    }
}