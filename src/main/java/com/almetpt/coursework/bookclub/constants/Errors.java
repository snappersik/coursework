package com.almetpt.coursework.bookclub.constants;

public interface Errors {

    class Books {
        public static final String BOOK_DELETED_ERROR = "Книга не может быть удаленна, т.к. имеются активные аренды";
    }

    class Authors {
        public static final String AUTHOR_DELETED_ERROR = "Автор не может быть удален, т.к. имеются активные аренды";

    }

    class Users {
        public static final String USER_FORBIDDEN_ERROR = "У вас нет прав просматривать информацию пользователей";

    }

    class REST {
        public static final String DELETED_ERROR_MESSAGE = "Удаление невозможно";
        public static final String AUTH_ERROR_MESSAGE = "Неавторизованный пользователь";
        public static final String ACCESS_ERROR_MESSAGE = "Отказано в доступе";
        public static final String NOT_FOUND_ERROR_MESSAGE = "Объект не найден";
    }

    class Products {
        public static final String PRODUCT_DELETED_ERROR = "Невозможно удалить продукт, так как он связан с существующими заказами.";
    }

    class Events {
        public static final String EVENT_NOT_FOUND = "Мероприятие не найдено";
        public static final String INVALID_QR_CODE = "Недействительный QR-код";
        public static final String EVENT_ALREADY_CANCELLED = "Мероприятие уже отменено";
        public static final String NO_CAPACITY = "Нет свободных мест";
    }

}
