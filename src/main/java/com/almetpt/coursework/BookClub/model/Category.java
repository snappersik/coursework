package com.almetpt.coursework.BookClub.model;

import lombok.Getter;

@Getter
public enum Category {
    ALL_ONLINE_MEETINGS_RECORDINGS("Все записи онлайн-встреч"), //TODO: {all-recordings} за {месяц} {год}
    SCIENCE_FICTION ("Подкаст"),
    MEETING_RECORD ("Запись встречи по книге");

    private final String categoryTextDisplay;

    Category(String categoryTextDisplay) {
        this.categoryTextDisplay = categoryTextDisplay;
    }

}

