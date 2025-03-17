package com.almetpt.coursework.BookClub.model;

import lombok.Getter;

@Getter
public enum ApplicationStatus {
    APPLICATION_APPROVED("Одобрена"),
    APPLICATION_REJECTED("Отклонена"), // если у заявки такй статус, то она не заносится в профиль
    APPLICATION_PENDING("На рассмотрении"),
    APPLICATION_CANCELED("Отменена"), // если у заявки такй статус, то она не заносится в профиль
    APPLICATION_EXPIRED("Истекла"); //TODO: если у заявки такй статус, то она не заносится в профиль

    private final String applicationStatusTextDisplay;

    ApplicationStatus(String categoryTextDisplay) {
            this.applicationStatusTextDisplay = categoryTextDisplay;
        }

}
