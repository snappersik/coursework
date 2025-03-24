package com.almetpt.coursework.bookclub.constants;

    public interface MailConstants {
        String MAIL_MESSAGE_FOR_REMEMBER_PASSWORD = """
                Добрый день. Вы получили это письмо, так как с вашего аккаунта была отправлена заявка на восстановление пароля.
                Для восстановления пароля перейдите по ссылке: http://localhost:8080/users/change-password?uuid=""";

        String MAIL_SUBJECT_FOR_REMEMBER_PASSWORD =
                "Восстановление пароля на сайте Онлайн Библиотека";

        String MAIL_SUBJECT_CONTACT = "письмо с обротной связи приложения Онлайн Библиотека";
        String MAIL_MESSAGE_CONTACT = "пришло новое сообщение с адреса: ";

    }

