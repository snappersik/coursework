package com.almetpt.coursework.bookclub.model;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("В обработке"),
    PROCESSING("Обрабатывается"), // Добавлено
    SHIPPED("Отправлен"),         // Добавлено
    COMPLETED("Завершен"),
    CANCELLED("Отменен");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }
}