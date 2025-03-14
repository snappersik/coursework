package com.almetpt.coursework.BookClub.model;

public enum OrderStatus {
    PENDING("Ожидает оплаты"),
    PROCESSING("Обрабатывается"),
    DELIVERED("Доставлен"),
    CANCELLED("Отменен");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
