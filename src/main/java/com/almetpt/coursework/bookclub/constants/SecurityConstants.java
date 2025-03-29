package com.almetpt.coursework.bookclub.constants;

import java.util.List;

public interface SecurityConstants {
    // URLs для REST API
    String USERS_URL = "/api/rest/users/**";
    String BOOKS_URL = "/api/rest/books/**";
    String PRODUCTS_URL = "/api/rest/products/**";
    String ORDERS_URL = "/api/rest/orders/**";
    String EVENTS_URL = "/api/rest/events/**";
    String EVENT_APPLICATIONS_URL = "/api/rest/event-applications/**";
    String CHECKIN_URL = "/api/checkin/**";

    // Публичные ресурсы
    List<String> RESOURCE_WHITE_LIST = List.of(
            "/resources/",
            "/static/",
            "/js/",
            "/css/",
            "/images/",
            "/carousel/",
            "/error",
            "/",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/webjars/**"
    );

    // Публичные URL для книг
    List<String> BOOK_WHITE_LIST = List.of(
            "/books",
            "/books/search",
            "/books/{id}"
    );

    // Публичные URL для пользователей
    List<String> USER_WHITE_LIST = List.of(
            "/login",
            "/users/registration",
            "/users/remember-password",
            "/users/change-password"
    );

    // Публичные URL для мероприятий
    List<String> EVENT_WHITE_LIST = List.of(
            "/events",
            "/events/search",
            "/events/{id}"
    );

    // Публичные URL для продуктов
    List<String> PRODUCT_WHITE_LIST = List.of(
            "/products",
            "/products/search",
            "/products/{id}"
    );

    // Защищенные URL
    List<String> PROTECTED_URLS = List.of(
            "/users/profile/**",
            "/rent/user-books/**",
            "/api/rest/event-applications/**",
            "/api/checkin/**",
            "/users/profile/**",
            "/api/users/**",
            "/api/carts/**",
            "/api/orders/**",
            "/api/event-applications/**"
    );

    // Разрешения для книг
    List<String> BOOK_PERMISSIONS_LIST = List.of(
            "/books/add",
            "/books/update",
            "/books/delete",
            "/books/download/{bookId}"
    );

    // Разрешения для организаторов
    List<String> ORGANIZER_PERMISSIONS_LIST = List.of(
            "/events/add",
            "/events/update",
            "/events/cancel",
            "/events/reschedule",
            "/event-applications/approve",
            "/event-applications/reject",
            "/checkin"
    );

    // Разрешения для пользователей
    List<String> USER_PERMISSIONS_LIST = List.of(
            "/books/read",
            "/products/read",
            "/orders/create",
            "/events/read",
            "/event-applications/create",
            "/cart/**"
    );

    // Разрешения для администраторов
    List<String> ADMIN_PERMISSIONS_LIST = List.of(
            "/users/add",
            "/users/update",
            "/users/delete",
            "/books/add",
            "/books/update",
            "/books/delete",
            "/products/add",
            "/products/update",
            "/products/delete",
            "/events/add",
            "/events/update",
            "/events/delete",
            "/events/cancel",
            "/events/reschedule",
            "/orders/update",
            "/orders/delete"
    );

    // Публичные URL для аутентификации
    List<String> AUTH_WHITE_LIST = List.of(
            "/api/auth/login",
            "/api/auth/register"
    );

}
