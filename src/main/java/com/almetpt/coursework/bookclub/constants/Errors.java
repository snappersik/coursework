package com.almetpt.coursework.bookclub.constants;

public interface Errors {

    class Books {
        public static final String BOOK_DELETED_ERROR = "Книга не может быть удаленна, т.к. имеются активные мероприятия с ней";
        public static final String BOOK_NOT_FOUND_ERROR = "Книга с id %d не найдена";
    }

    class Users {
        public static final String USER_FORBIDDEN_ERROR = "У вас нет прав просматривать информацию пользователей";
        public static final String USER_NOT_FOUND_ERROR = "Пользователь с id %d не найден";
    }

    class REST {
        public static final String DELETED_ERROR_MESSAGE = "Удаление невозможно";
        public static final String AUTH_ERROR_MESSAGE = "Неавторизованный пользователь";
        public static final String ACCESS_ERROR_MESSAGE = "Отказано в доступе";
        public static final String NOT_FOUND_ERROR_MESSAGE = "Объект не найден";
    }

    class Products {
        public static final String PRODUCT_DELETE_FORBIDDEN_ERROR = "Невозможно удалить продукт, так как он связан с существующими заказами.";
        public static final String PRODUCT_NOT_FOUND_ERROR = "Продукт с id %d не найден";
    }

    class Events {
        public static final String EVENT_NOT_FOUND = "Мероприятие с id %d не найдено";
        public static final String INVALID_QR_CODE = "Недействительный QR-код";
        public static final String EVENT_ALREADY_CANCELLED = "Мероприятие уже отменено";
        public static final String NO_CAPACITY = "Нет свободных мест";
        public static final String EVENT_DELETED_ERROR = "Мероприятие не может быть удалено, т.к. на него есть заявки";
    }
    
    class EventApplications {
        // public static final String APPLICATION_NOT_FOUND_ERROR = "Заявка на мероприятие с id %d не найдена";
    }
    
    class Orders {
        public static final String ORDER_NOT_FOUND_ERROR = "Заказ с id %d не найден";
    }
    
    class Carts {
        public static final String CART_NOT_FOUND_ERROR = "Корзина с id %d не найдена";
    }
}
