package com.almetpt.coursework.BookClub.model;

import lombok.Getter;

@Getter
public enum Genre {
    FANTASY ("Фантастика"),
    SCIENCE_FICTION ("Научная фантастика"),
    DRAMA ("Драма"),
    NOVEL("Роман"),
    COMEDY("Комедия"),
    ADVENTURE ("Приключения");

    private final String genreTextDisplay;

    Genre(String genreTextDisplay) {
        this.genreTextDisplay = genreTextDisplay;
    }

}
