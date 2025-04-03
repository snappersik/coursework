package com.almetpt.coursework.bookclub.constants;

import java.util.List;

public interface SecurityConstants {
    // Базовые пути API
    String API_PATH = "/api/rest";

    // Публичные ресурсы
    List<String> RESOURCE_WHITE_LIST = List.of(
            "/",
            "/error",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/webjars/**",
            "/resources/**",
            "/static/**"
    );

    // Пути аутентификации
    List<String> AUTH_WHITE_LIST = List.of(
            API_PATH + "/auth/login",
            API_PATH + "/auth/register",
            API_PATH + "/auth/refresh"
    );

    // Публичные GET-запросы
    List<String> PUBLIC_GET_LIST = List.of(
            API_PATH + "/books/**",
            API_PATH + "/products/**",
            API_PATH + "/events/**"
    );

    // Права администратора
    List<String> ADMIN_PERMISSIONS_LIST = List.of(
            API_PATH + "/users/**",
            API_PATH + "/books/**",
            API_PATH + "/products/**",
            API_PATH + "/orders/**",
            API_PATH + "/events/**",
            API_PATH + "/event-applications/**"
    );

    // Права организатора
    List<String> ORGANIZER_PERMISSIONS_LIST = List.of(
            API_PATH + "/events",
            API_PATH + "/events/*",
            API_PATH + "/events/*/cancel",
            API_PATH + "/events/*/reschedule",
            API_PATH + "/event-applications",
            API_PATH + "/event-applications/*/approve",
            API_PATH + "/event-applications/*/reject"
    );

    // Общие права авторизованных пользователей
    List<String> AUTHENTICATED_PERMISSIONS = List.of(
            API_PATH + "/cart/**",
            API_PATH + "/orders",
            API_PATH + "/profile/**"
    );
}
