package com.almetpt.coursework.bookclub.model;

import lombok.Getter;

@Getter
public enum ApplicationStatus {
    PENDING("Подана"),
    APPROVED("Одобрена"),
    REJECTED("Отклонена");

    private final String description;

    ApplicationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
