package com.almetpt.coursework.library.constants;

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

    class REST{
        public static final String DELETED_ERROR_MESSAGE = "Удаление невозможно";
        public static final String AUTH_ERROR_MESSAGE = "Неавторизованный пользователь";
        public static final String ACCESS_ERROR_MESSAGE ="Отказано в доступе";
        public static final String NOT_FOUND_ERROR_MESSAGE ="Объект не найден";
    }
}

