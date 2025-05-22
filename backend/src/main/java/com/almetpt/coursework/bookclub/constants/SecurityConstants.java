package com.almetpt.coursework.bookclub.constants;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface SecurityConstants {

        // Базовые пути API
        String API_PATH = "/api/rest";

        // Метод для формирования списков с префиксом API_PATH
        static List<String> prefixPaths(String... paths) {
                return Arrays.stream(paths)
                                .map(path -> API_PATH + path)
                                .collect(Collectors.toList());
        }

        // Публичные ресурсы
        List<String> RESOURCES_WHITE_LIST = List.of(
                        "/resources/**",
                        "/static/**",
                        "/js/**",
                        "/css/**",
                        "/images/**",
                        "/carousel/**",
                        "/error",
                        "/",
                        "/swagger-ui/**",
                        "/webjars/bootstrap/5.3.3/**",
                        "/v3/api-docs/**");

        // Пути аутентификации
        List<String> AUTH_WHITE_LIST = prefixPaths(
                        "/auth/login",
                        "/auth/register",
                        "/auth/logout",
                        "/auth/forgot-password",
                        "/auth/reset-password",
                        "/auth/refresh",
                        "/auth/validate-reset-token");

        // Публичные GET-запросы для чтения данных
        List<String> PUBLIC_GET_LIST = prefixPaths(
                        "/books",
                        "/books/*",
                        "/books/*/cover",
                        "/books/*/image-info",
                        "/slider/books",
                        "/products",
                        "/products/*",
                        "/products/all",
                        "/products/categories",
                        "/products/categories/with-descriptions",
                        "/events",
                        "/events/*");

        // Swagger документация
        List<String> SWAGGER_RESOURCES = List.of(
                        "/swagger-ui/**",
                        "/v3/api-docs/**");

        // Доступ к обложкам книг
        List<String> BOOK_COVERS = prefixPaths(
                        "/books/*/cover");

        // Доступ к профилю пользователя
        List<String> USER_PROFILE = prefixPaths(
                        "/users/profile",
                        "/event-applications/{id}/cancel");

        // Доступ к аудиту
        List<String> AUDIT_PATHS = prefixPaths(
                        "/audit");

        // Пути для админ-панели
        List<String> ADMIN_PANEL = prefixPaths(
                        "/admin/**");

        List<String> ORGANIZER_DASHBOARD_ACCESS = prefixPaths(
                        "/admin/stats",
                        "/admin/stats/events");

        // Права для управления книгами
        List<String> BOOKS_MANAGEMENT_LIST = prefixPaths(
                        "/books/create",
                        "/books/*/update",
                        "/books/*/delete",
                        "/books/soft/*",
                        "/books/all",
                        "/books/restore/*");

        // Права для управления продуктами
        List<String> PRODUCTS_MANAGEMENT_LIST = prefixPaths(
                        "/products",
                        "/products/create",
                        "/products/*/update",
                        "/products/{id}/upload",
                        "/products/*/delete",
                        "/products/soft/*",
                        "/products/all",
                        "/products/{id}/with-file",
                        "/products/restore/*");

        List<String> PRODUCT_CATEGORIES_LIST = prefixPaths(
                        "/categories",
                        "/categories/with-descriptions");

        // Права для управления мероприятиями (для админа)
        List<String> EVENTS_ADMIN_MANAGEMENT_LIST = prefixPaths(
                        "/events/all",
                        "/events/*/delete",
                        "/events/soft/*",
                        "/events/restore/*",
                        "/events/create",
                        "/events/*/update",
                        "/events/*/cancel",
                        "/events/*/reschedule");

        // Права для управления мероприятиями (для организатора)
        List<String> EVENTS_ORGANIZER_MANAGEMENT_LIST = prefixPaths(
                        "/events/create",
                        "/events/*/update",
                        "/events/*/cancel",
                        "/events/*/reschedule");

        // Права для управления заявками на мероприятия (для админа)
        List<String> EVENT_APPLICATIONS_ADMIN_LIST = prefixPaths(
                        "/event-applications/paginated",
                        "/event-applications/all",
                        "/event-applications/*/delete",
                        "/event-applications/soft/*",
                        "/event-applications/restore/*",
                        "/event-applications/*/approve",
                        "/event-applications/*/reject",
                        "/event-applications/mark-attended/*");

        // Права для управления заявками на мероприятия (для организатора)
        List<String> EVENT_APPLICATIONS_ORGANIZER_LIST = prefixPaths(
                        "/event-applications",
                        "/event-applications/*/approve",
                        "/event-applications/*/reject",
                        "/event-applications/mark-attended/*");

        List<String> ORGANIZER_DASHBOARD = prefixPaths(
                        "/admin/stats",
                        "/admin/stats/events");

        // Права для управления check-in (для админа и организатора)
        List<String> CHECK_IN_MANAGEMENT_LIST = prefixPaths(
                        "/audit/",
                        "/checkin/**");

        // Права для управления заказами (для админа)
        List<String> ORDERS_ADMIN_LIST = prefixPaths(
                        "/orders/*/update",
                        "/orders/*/delete",
                        "/orders/restore",
                        "/orders/soft/*",
                        "/orders/restore/*");

        // Пути для заказов с HTTP методами (GET)
        List<String> ORDERS_GET_ALL = prefixPaths(
                        "/orders/all",
                        "/orders/",
                        "/orders",
                        "/orders/{id}",
                        "/orders/*",
                        "/orders/{id}/status",
                        "/orders/{orderId}",
                        "/orders/{id}/*",
                        "/orders/{orderId}/*",
                        "/orders/create");

        // Пути для заказов с HTTP методами (PUT)
        List<String> ORDERS_PUT_STATUS = prefixPaths(
                        "/orders/*/status");

        // Пути для заказов с HTTP методами (DELETE)
        List<String> ORDERS_DELETE = prefixPaths(
                        "/orders/*/delete");

        // Пути для заказов с HTTP методами (POST)
        List<String> ORDERS_POST_SOFT = prefixPaths(
                        "/orders/soft/*");

        // Пути для заказов с HTTP методами (PATCH)
        List<String> ORDERS_PATCH_RESTORE = prefixPaths(
                        "/orders/restore/*");

        // Пути для заказов пользователей
        List<String> ORDERS_USER_PATHS = prefixPaths(
                        "/orders/my",
                        "/orders/create");

        // Пути для изображений книг (только для администраторов)
        List<String> BOOK_IMAGES_ADMIN_LIST = prefixPaths(
                        "/books/*/upload",
                        "/books/*/upload-binary",
                        "/books/url");

        // Права для управления пользователями (только для админа)
        List<String> USERS_MANAGEMENT_LIST = prefixPaths(
                        "/users",
                        "/users/*",
                        "/users/create",
                        "/users/*/update",
                        "/users/*/delete");

        // Общие права авторизованных пользователей
        List<String> AUTHENTICATED_PERMISSIONS = prefixPaths(
                        "/carts/**",
                        "/orders/create",
                        "/orders/my",
                        "/profile/**",
                        "/users/profile",
                        "/users/profile/avatar-upload",
                        "/event-applications/create",
                        "/event-applications/my",
                        "/event-applications/{id}/cancel");

        // Объединение всех прав администратора
        List<String> ADMIN_PERMISSIONS_LIST = Stream.of(
                        BOOKS_MANAGEMENT_LIST,
                        BOOK_IMAGES_ADMIN_LIST,
                        PRODUCTS_MANAGEMENT_LIST,
                        EVENTS_ADMIN_MANAGEMENT_LIST,
                        EVENT_APPLICATIONS_ADMIN_LIST,
                        ORDERS_ADMIN_LIST,
                        USERS_MANAGEMENT_LIST,
                        CHECK_IN_MANAGEMENT_LIST,
                        ADMIN_PANEL,
                        AUDIT_PATHS)
                        .flatMap(List::stream)
                        .collect(Collectors.toList());

        // Объединение всех прав организатора
        List<String> ORGANIZER_PERMISSIONS_LIST = Stream.of(
                        BOOKS_MANAGEMENT_LIST,
                        PRODUCTS_MANAGEMENT_LIST,
                        EVENTS_ORGANIZER_MANAGEMENT_LIST,
                        EVENT_APPLICATIONS_ORGANIZER_LIST,
                        CHECK_IN_MANAGEMENT_LIST,
                        ORGANIZER_DASHBOARD)
                        .flatMap(List::stream)
                        .collect(Collectors.toList());

}
