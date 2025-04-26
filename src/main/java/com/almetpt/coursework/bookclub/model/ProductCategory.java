package com.almetpt.coursework.bookclub.model;

import lombok.Getter;

@Getter
public enum ProductCategory {
    BOOK("Книга"),
    AUDIO_BOOK("Аудиокнига"),
    E_BOOK("Электронная книга"),
    MERCHANDISE("Сувенирная продукция"),
    GIFT_CARD("Подарочная карта");

    private final String description;

    ProductCategory(String description) {
        this.description = description;
    }

}
