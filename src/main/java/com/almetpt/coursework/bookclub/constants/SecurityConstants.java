package com.almetpt.coursework.bookclub.constants;

import java.util.List;

public interface SecurityConstants {
    String SECRET_KEY = "bookclub_secret_key";
    String TOKEN_PREFIX = "Bearer ";
    String HEADER_STRING = "Authorization";
    String SIGN_UP_URL = "/api/rest/users/registration";
    String LOGIN_URL = "/api/rest/users/auth";
    long EXPIRATION_TIME = 900_000; // 15 минут

    // URLs
    String USERS_URL = "/api/rest/users/**";
    String BOOKS_URL = "/api/rest/books/**";
    String PRODUCTS_URL = "/api/rest/products/**";
    String ORDERS_URL = "/api/rest/orders/**";
    String EVENTS_URL = "/api/rest/events/**";
    String EVENT_APPLICATIONS_URL = "/api/rest/event-applications/**";
    String CHECKIN_URL = "/api/checkin/**";

    List<String> RESOURCE_WHITE_LIST = List.of(
            "/resources/",
            "/static/",
            "/js/",
            "/css/",
            "/images/",
            "/carousel/",
            "/error",
            "/",
            "/swagger-ui/",
            "/webjars/bootstrap/5.3.3/",
            "/v3/api-docs/**");

    List<String> BOOK_WHITE_LIST = List.of(
            "/books",
            "/books/search",
            "/books/{id}");

    List<String> BOOK_PERMISSIONS_LIST = List.of(
            "/books/add",
            "/books/update",
            "/books/delete",
            "/books/download/{bookId}");

    List<String> USER_WHITE_LIST = List.of(
            "/login",
            "/users/registration",
            "/users/remember-password",
            "/users/change-password",
            "/users/profile/**",
            "/rent/user-books/**"
    );

    List<String> ORGANIZER_PERMISSIONS_LIST = List.of(
            "/events/add",
            "/events/update",
            "/events/cancel",
            "/events/reschedule",
            "/event-applications/approve",
            "/event-applications/reject",
            "/checkin"
    );

    List<String> USER_PERMISSIONS_LIST = List.of(
            "/books/read",
            "/products/read",
            "/orders/create",
            "/events/read",
            "/event-applications/create"
    );
}