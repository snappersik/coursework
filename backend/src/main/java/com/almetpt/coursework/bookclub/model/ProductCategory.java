package com.almetpt.coursework.bookclub.model;

import lombok.Getter;

@Getter
public enum ProductCategory {
    E_BOOK("Электронная книга"),
    AUDIO_BOOK("Аудиокнига"),
    LECTURE("Лекция"),
    MEETING_RECORDING("Записи прошлых встреч");

    private final String description;

    ProductCategory(String description) {
        this.description = description;
    }

    // Метод для получения Enum по строковому значению (полезно для контроллера)
    public static ProductCategory fromString(String text) {
        for (ProductCategory b : ProductCategory.values()) {
            if (b.name().equalsIgnoreCase(text) || b.description.equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}
