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
                        "/auth/refresh");

        // Публичные GET-запросы для чтения данных
        List<String> PUBLIC_GET_LIST = prefixPaths(
                        "/books",
                        "/books/*",
                        "/products",
                        "/products/*",
                        "/events",
                        "/events/*");

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
                        "/products/create",
                        "/products/*/update",
                        "/products/*/delete",
                        "/products/soft/*",
                        "/products/all",
                        "/products/restore/*");

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
        // Организатор может создавать, обновлять, отменять и переносить мероприятия,
        // но не может полностью удалять или восстанавливать удаленные мероприятия
        List<String> EVENTS_ORGANIZER_MANAGEMENT_LIST = prefixPaths(
                        "/events/create",
                        "/events/*/update",
                        "/events/*/cancel",
                        "/events/*/reschedule");

        // Права для управления заявками на мероприятия (для админа)
        List<String> EVENT_APPLICATIONS_ADMIN_LIST = prefixPaths(
                        "/event-applications/all",
                        "/event-applications/*/delete",
                        "/event-applications/soft/*",
                        "/event-applications/restore/*",
                        "/event-applications/*/approve",
                        "/event-applications/*/reject",
                        "/event-applications/mark-attended/*");

        // Права для управления заявками на мероприятия (для организатора)
        // Организатор может просматривать, одобрять, отклонять заявки
        // и отмечать посещаемость
        List<String> EVENT_APPLICATIONS_ORGANIZER_LIST = prefixPaths(
                        "/event-applications",
                        "/event-applications/*/approve",
                        "/event-applications/*/reject",
                        "/event-applications/mark-attended/*");

        // Права для управления заказами (для админа)
        List<String> ORDERS_ADMIN_LIST = prefixPaths(
                        "/orders/all",
                        "/orders/*/update",
                        "/orders/*/delete",
                        "/orders/soft/*",
                        "/orders/restore/*");

        // Права для управления пользователями (только для админа)
        List<String> USERS_MANAGEMENT_LIST = prefixPaths(
                        "/users",
                        "/users/*",
                        "/users/create",
                        "/users/*/update",
                        "/users/*/delete");

        // Общие права авторизованных пользователей
        List<String> AUTHENTICATED_PERMISSIONS = prefixPaths(
                        "/cart/**",
                        "/orders/create",
                        "/orders/my",
                        "/profile/**",
                        "/auth/logout",
                        "/event-applications/create",
                        "/event-applications/my");

        // Объединение всех прав администратора
        // Админ имеет все права в системе, кроме доступа к корзинам пользователей
        List<String> ADMIN_PERMISSIONS_LIST = Stream.of(
                        BOOKS_MANAGEMENT_LIST,
                        PRODUCTS_MANAGEMENT_LIST,
                        EVENTS_ADMIN_MANAGEMENT_LIST,
                        EVENT_APPLICATIONS_ADMIN_LIST,
                        ORDERS_ADMIN_LIST,
                        USERS_MANAGEMENT_LIST).flatMap(List::stream).collect(Collectors.toList());

        // Объединение всех прав организатора
        // Организатор имеет права на управление книгами, мероприятиями и заявками,
        // но не имеет доступа к управлению пользователями, заказами и корзинами
        List<String> ORGANIZER_PERMISSIONS_LIST = Stream.of(
                        BOOKS_MANAGEMENT_LIST,
                        PRODUCTS_MANAGEMENT_LIST, // Добавлено право на управление продуктами
                        EVENTS_ORGANIZER_MANAGEMENT_LIST,
                        EVENT_APPLICATIONS_ORGANIZER_LIST).flatMap(List::stream).collect(Collectors.toList());

}
