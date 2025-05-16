package com.almetpt.coursework.bookclub.model;

import lombok.Getter;

@Getter
public enum BookGenre {
    FICTION("Художественная литература"),
    NON_FICTION("Нон-фикшн"),
    SCIENCE_FICTION("Научная фантастика"),
    FANTASY("Фэнтези"),
    MYSTERY("Детектив"),
    THRILLER("Триллер"),
    HORROR("Ужасы"),
    HISTORICAL_FICTION("Историческая проза"),
    ROMANCE("Любовный роман"),
    WESTERN("Вестерн"),
    BIOGRAPHY("Биография"),
    AUTOBIOGRAPHY("Автобиография"),
    MEMOIR("Мемуары"),
    SELF_HELP("Саморазвитие"),
    BUSINESS("Бизнес литература"),
    TRAVEL("Путешествия"),
    CHILDREN("Детская литература"),
    POETRY("Поэзия"),
    DRAMA("Драма"),
    CLASSICS("Классическая литература");

    private final String description;

    BookGenre(String description) {
        this.description = description;
    }

}

