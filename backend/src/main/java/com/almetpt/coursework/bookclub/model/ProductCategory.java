package com.almetpt.coursework.bookclub.model;

import lombok.Getter;

@Getter
public enum ProductCategory {
    AUDIO_BOOK("Аудиокнига"),
    E_BOOK("Электронная книга");
    // добавить категорий

    private final String description;

    ProductCategory(String description) {
        this.description = description;
    }

}
