package com.almetpt.coursework.BookClub.model;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("Ожидает оплаты"),
    PROCESSING("Обрабатывается"),
    DELIVERED("Доставлен"),
    CANCELLED("Отменен");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

}
