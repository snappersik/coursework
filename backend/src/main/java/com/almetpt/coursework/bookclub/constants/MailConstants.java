package com.almetpt.coursework.bookclub.constants;

public interface MailConstants {

    String FRONTEND_BASE_URL_PROPERTY = "${frontend.base.url:http://localhost:3000}";

    String MAIL_SUBJECT_FOR_PASSWORD_RESET = "Восстановление пароля для вашего аккаунта Книжного Клуба";

    String MAIL_MESSAGE_FOR_PASSWORD_RESET_LINK_TEMPLATE = """
            Добрый день!
            Вы (или кто-то другой) запросили сброс пароля для вашего аккаунта в книжном клубе "Книжная Гавань".
            Если это были не вы, просто проигнорируйте это письмо.
            Чтобы сбросить пароль, перейдите по следующей ссылке:
            %s/reset-password?token=%s
            Эта ссылка действительна в течение 24 часов.
            
            С уважением,
            Команда Книжного Клуба
            """;

    String MAIL_SUBJECT_CONTACT = "письмо с обротной связи приложения Онлайн Библиотека";
    String MAIL_MESSAGE_CONTACT = "пришло новое сообщение с адреса: ";
}
