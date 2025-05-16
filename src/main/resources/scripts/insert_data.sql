
SELECT * FROM audit_entries ORDER BY timestamp DESC LIMIT 10;

ALTER SEQUENCE products_seq RESTART WITH 1;




-- Сброс последовательности default_generator
ALTER SEQUENCE public.default_generator INCREMENT 1;
ALTER SEQUENCE default_generator RESTART WITH 1;

-- Создание и настройка последовательностей для всех таблиц
DROP SEQUENCE IF EXISTS user_seq;
CREATE SEQUENCE user_seq START WITH 1 INCREMENT BY 1;
ALTER SEQUENCE user_seq OWNER TO postgres;

DROP SEQUENCE IF EXISTS cart_seq;
CREATE SEQUENCE cart_seq START WITH 1 INCREMENT BY 1;
ALTER SEQUENCE cart_seq OWNER TO postgres;

DROP SEQUENCE IF EXISTS book_seq;
CREATE SEQUENCE book_seq START WITH 1 INCREMENT BY 1;
ALTER SEQUENCE book_seq OWNER TO postgres;

DROP SEQUENCE IF EXISTS event_seq;
CREATE SEQUENCE event_seq START WITH 1 INCREMENT BY 1;
ALTER SEQUENCE event_seq OWNER TO postgres;

DROP SEQUENCE IF EXISTS product_seq;
CREATE SEQUENCE product_seq START WITH 1 INCREMENT BY 1;
ALTER SEQUENCE product_seq OWNER TO postgres;

DROP SEQUENCE IF EXISTS order_seq;
CREATE SEQUENCE order_seq START WITH 1 INCREMENT BY 1;
ALTER SEQUENCE order_seq OWNER TO postgres;

DROP SEQUENCE IF EXISTS event_application_seq;
CREATE SEQUENCE event_application_seq START WITH 1 INCREMENT BY 1;
ALTER SEQUENCE event_application_seq OWNER TO postgres;

-- Очистка таблиц для избежания конфликтов
TRUNCATE TABLE event_applications CASCADE;
TRUNCATE TABLE orders CASCADE;
TRUNCATE TABLE cart_products CASCADE;
TRUNCATE TABLE order_products CASCADE;
TRUNCATE TABLE carts CASCADE;
TRUNCATE TABLE events CASCADE;
TRUNCATE TABLE products CASCADE;
TRUNCATE TABLE books CASCADE;
TRUNCATE TABLE users CASCADE;

-- Сброс и настройка последовательности default_generator
ALTER SEQUENCE default_generator INCREMENT 1;
ALTER SEQUENCE default_generator MINVALUE 1;
ALTER SEQUENCE default_generator RESTART WITH 1;

-- Очистка таблиц для избежания конфликтов
TRUNCATE TABLE carts CASCADE;
TRUNCATE TABLE users CASCADE;






---- Вставка книг
--INSERT INTO books (id, created_by, created_when, is_deleted, title, author, genre, description, is_reading, publication_date)
--VALUES (nextval('book_seq'), 'SYSTEM', CURRENT_TIMESTAMP, false, 'Мастер и Маргарита', 'Михаил Булгаков', 'FICTION', 'Роман о дьяволе, который посетил Москву', true, '1966-01-01');
--
--INSERT INTO books (id, created_by, created_when, is_deleted, title, author, genre, description, is_reading, publication_date)
--VALUES (nextval('book_seq'), 'SYSTEM', CURRENT_TIMESTAMP, false, 'Преступление и наказание', 'Федор Достоевский', 'FICTION', 'Психологический роман о преступлении и его последствиях', false, '1866-01-01');
--
---- Вставка пользователей
--INSERT INTO users (id, created_by, created_when, is_deleted, email, password, first_name, last_name, patronymic, address, phone, birth_date, role_id)
--VALUES (nextval('user_seq'), 'SYSTEM', CURRENT_TIMESTAMP, false, 'user@example.com', '$2a$10$/K4nznrj3/UspoqUFUuEqeKxzm85y3J.PXXgzyKJVMEbj1F8QaE/2', 'Иван', 'Иванов', 'Иванович', 'Москва', '+71234567890', '2000-01-01', 1);
--
--INSERT INTO users (id, created_by, created_when, is_deleted, email, password, first_name, last_name, patronymic, address, phone, birth_date, role_id)
--VALUES (nextval('user_seq'), 'SYSTEM', CURRENT_TIMESTAMP, false, 'admin@example.com', '$2a$10$/K4nznrj3/UspoqUFUuEqeKxzm85y3J.PXXgzyKJVMEbj1F8QaE/2', 'Админ', 'Админов', 'Админович', 'Санкт-Петербург', '+79876543210', '1990-01-01', 3);
--
---- Вставка корзин для пользователей
--INSERT INTO carts (id, created_by, created_when, is_deleted, user_id)
--VALUES (nextval('cart_seq'), 'SYSTEM', CURRENT_TIMESTAMP, false, 1);
--
--INSERT INTO carts (id, created_by, created_when, is_deleted, user_id)
--VALUES (nextval('cart_seq'), 'SYSTEM', CURRENT_TIMESTAMP, false, 2);
--
---- Вставка продуктов
--INSERT INTO products (id, created_by, created_when, is_deleted, name, description, price, category)
--VALUES (nextval('product_seq'), 'SYSTEM', CURRENT_TIMESTAMP, false, 'Закладка для книг', 'Красивая закладка с цитатами', 150.00, 'ACCESSORY');
--
--INSERT INTO products (id, created_by, created_when, is_deleted, name, description, price, category)
--VALUES (nextval('product_seq'), 'SYSTEM', CURRENT_TIMESTAMP, false, 'Чехол для книг', 'Защитный чехол для книг', 350.00, 'ACCESSORY');
--
---- Вставка мероприятий
--INSERT INTO events (id, created_by, created_when, is_deleted, title, event_type, date, description, max_participants, is_cancelled, book_id)
--VALUES (nextval('event_seq'), 'SYSTEM', CURRENT_TIMESTAMP, false, 'Обсуждение "Мастера и Маргариты"', 'DISCUSSION', '2025-04-15 18:00:00', 'Обсуждение романа М. Булгакова', 20, false, 1);
--
--INSERT INTO events (id, created_by, created_when, is_deleted, title, event_type, date, description, max_participants, is_cancelled, book_id)
--VALUES (nextval('event_seq'), 'SYSTEM', CURRENT_TIMESTAMP, false, 'Лекция о Достоевском', 'LECTURE', '2025-05-20 19:00:00', 'Лекция о творчестве Ф.М. Достоевского', 30, false, 2);
--
---- Вставка заявок на мероприятия
--INSERT INTO event_applications (id, created_by, created_when, is_deleted, user_id, event_id, status, attended, qr_code)
--VALUES (nextval('event_application_seq'), 'SYSTEM', CURRENT_TIMESTAMP, false, 1, 1, 'APPROVED', false, 'qr_code_data_1');
--
---- Вставка заказов
--INSERT INTO orders (id, created_by, created_when, is_deleted, user_id, total, status)
--VALUES (nextval('order_seq'), 'SYSTEM', CURRENT_TIMESTAMP, false, 1, 500.00, 'PENDING');
--
---- Связь заказов с продуктами
--INSERT INTO order_products (order_id, product_id)
--VALUES (1, 1);
--
--INSERT INTO order_products (order_id, product_id)
--VALUES (1, 2);
--
---- Проверка текущих значений последовательностей
--SELECT 'user_seq' as sequence_name, last_value FROM user_seq
--UNION ALL
--SELECT 'cart_seq', last_value FROM cart_seq
--UNION ALL
--SELECT 'book_seq', last_value FROM book_seq
--UNION ALL
--SELECT 'event_seq', last_value FROM event_seq
--UNION ALL
--SELECT 'product_seq', last_value FROM product_seq
--UNION ALL
--SELECT 'order_seq', last_value FROM order_seq
--UNION ALL
--SELECT 'event_application_seq', last_value FROM event_application_seq;
