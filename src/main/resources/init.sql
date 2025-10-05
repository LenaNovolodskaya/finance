-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Хост: 127.0.0.1
-- Время создания: Май 26 2025 г., 12:32
-- Версия сервера: 10.4.32-MariaDB
-- Версия PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- База данных: `finance`
--

-- --------------------------------------------------------

--
-- Структура таблицы `category`
--

CREATE TABLE `category` (
  `custom` bit(1) NOT NULL,
  `id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `type` enum('EDUCATION','ENTERTAINMENT','FOOD','HEALTH','OTHER','SALARY','TECHNIC','TRANSPORT') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- Дамп данных таблицы `category`
--

INSERT INTO `category` (`custom`, `id`, `user_id`, `name`, `type`) VALUES
(b'0', 1, NULL, 'Еда', 'FOOD'),
(b'0', 2, NULL, 'Здоровье', 'HEALTH'),
(b'0', 3, NULL, 'Образование', 'EDUCATION'),
(b'0', 4, NULL, 'Транспорт', 'TRANSPORT'),
(b'0', 5, NULL, 'Развлечения', 'ENTERTAINMENT'),
(b'0', 6, NULL, 'Техника', 'TECHNIC'),
(b'0', 7, NULL, 'Зарплата', 'SALARY'),
(b'1', 8, 2, 'Праздник', 'OTHER'),
(b'1', 9, 2, 'Магазин', 'OTHER'),
(b'1', 10, 3, 'Стипендия', 'OTHER'),
(b'1', 11, 3, 'Материальная помощь', 'OTHER'),
(b'1', 13, NULL, 'Долги', 'OTHER'),
(b'1', 14, 2, 'Путешествие', 'OTHER'),
(b'1', 15, 2, 'Аванс', 'OTHER'),
(b'1', 16, 2, 'Подработка', 'OTHER'),
(b'1', 17, 2, 'Жильё', 'OTHER'),
(b'1', 18, 3, 'Магазин', 'OTHER'),
(b'1', 19, 3, 'Праздник', 'OTHER'),
(b'1', 27, 2, 'Мобильная связь', 'OTHER');

-- --------------------------------------------------------

--
-- Структура таблицы `debt`
--

CREATE TABLE `debt` (
  `amount` decimal(38,2) NOT NULL,
  `due_date` date NOT NULL,
  `id` int(11) NOT NULL,
  `lent` bit(1) NOT NULL,
  `remaining_amount` decimal(38,2) DEFAULT NULL,
  `returned` bit(1) NOT NULL,
  `user_id` int(11) NOT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `contact_name` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- Дамп данных таблицы `debt`
--

INSERT INTO `debt` (`amount`, `due_date`, `id`, `lent`, `remaining_amount`, `returned`, `user_id`, `comment`, `contact_name`) VALUES
(2000.00, '2025-05-29', 7, b'0', 2000.00, b'0', 2, '', 'Аня'),
(1500.00, '2025-05-20', 9, b'1', 1500.00, b'0', 2, '', 'Катя'),
(3000.00, '2025-05-30', 12, b'1', 0.00, b'1', 2, '', 'Оля'),
(1500.00, '2025-05-29', 13, b'1', 1500.00, b'0', 3, '', 'Саша'),
(1500.00, '2025-05-29', 19, b'0', 0.00, b'1', 2, '', 'Саша'),
(1000.00, '2025-05-31', 20, b'1', 1000.00, b'0', 2, '', 'Оля');

-- --------------------------------------------------------

--
-- Структура таблицы `goal`
--

CREATE TABLE `goal` (
  `category_id` int(11) NOT NULL,
  `completed` bit(1) NOT NULL,
  `current_amount` decimal(38,2) NOT NULL,
  `id` int(11) NOT NULL,
  `monthly_contribution` decimal(38,2) NOT NULL,
  `target_amount` decimal(38,2) NOT NULL,
  `target_date` date NOT NULL,
  `user_id` int(11) NOT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `title` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- Дамп данных таблицы `goal`
--

INSERT INTO `goal` (`category_id`, `completed`, `current_amount`, `id`, `monthly_contribution`, `target_amount`, `target_date`, `user_id`, `comment`, `title`) VALUES
(6, b'0', 10000.00, 2, 0.00, 20000.00, '2025-05-16', 2, '', 'Покупка телефона'),
(8, b'1', 5000.00, 3, 0.00, 5000.00, '2026-07-01', 2, '', 'Подарок маме'),
(14, b'0', 20000.00, 6, 8333.34, 120000.00, '2026-06-01', 2, '', 'Поездка в Питер'),
(19, b'0', 3000.00, 11, 3500.00, 10000.00, '2025-08-03', 3, '', 'День рождения'),
(3, b'0', 5000.00, 18, 6250.00, 30000.00, '2025-10-01', 2, '', 'Курсы по английскому'),
(6, b'0', 0.00, 19, 7272.73, 80000.00, '2026-05-16', 2, '', 'Покупка ноутбука');

-- --------------------------------------------------------

--
-- Структура таблицы `transaction`
--

CREATE TABLE `transaction` (
  `amount` decimal(38,2) NOT NULL,
  `category_id` int(11) NOT NULL,
  `date` date NOT NULL,
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `type` enum('EXPENSE','INCOME') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- Дамп данных таблицы `transaction`
--

INSERT INTO `transaction` (`amount`, `category_id`, `date`, `id`, `user_id`, `comment`, `type`) VALUES
(60000.00, 7, '2025-05-01', 1, 2, '', 'INCOME'),
(2000.00, 8, '2025-05-11', 2, 2, 'День рождения подруги', 'EXPENSE'),
(1500.00, 9, '2025-05-10', 3, 2, '', 'EXPENSE'),
(10000.00, 10, '2025-04-25', 5, 3, '', 'INCOME'),
(4000.00, 11, '2025-04-25', 6, 3, '', 'INCOME'),
(500.00, 1, '2025-05-02', 13, 2, 'КФС', 'EXPENSE'),
(400.00, 1, '2025-05-03', 14, 2, 'Бургер Кинг', 'EXPENSE'),
(460.00, 1, '2025-05-04', 15, 2, 'Вкусно и точка', 'EXPENSE'),
(600.00, 5, '2025-05-03', 16, 2, 'Кинотеатр', 'EXPENSE'),
(5000.00, 8, '2025-05-21', 17, 2, 'Вклад в цель: Подарок маме', 'EXPENSE'),
(10000.00, 6, '2025-05-02', 18, 2, 'Вклад в цель: Покупка телефона', 'EXPENSE'),
(10000.00, 14, '2025-05-02', 20, 2, 'Вклад в цель: Поездка в Питер', 'EXPENSE'),
(10000.00, 2, '2025-05-05', 22, 2, 'Стоматолог', 'EXPENSE'),
(15000.00, 15, '2025-05-20', 23, 2, '', 'INCOME'),
(200.00, 4, '2025-05-05', 24, 2, '', 'EXPENSE'),
(1000.00, 1, '2025-05-05', 25, 2, 'Антрикот', 'EXPENSE'),
(2000.00, 9, '2025-05-03', 26, 2, 'Слата', 'EXPENSE'),
(620.00, 1, '2025-05-07', 27, 2, 'Wowa лапша', 'EXPENSE'),
(300.00, 4, '2025-05-06', 28, 2, '', 'EXPENSE'),
(240.00, 4, '2025-05-07', 29, 2, '', 'EXPENSE'),
(360.00, 4, '2025-05-08', 30, 2, '', 'EXPENSE'),
(310.00, 4, '2025-05-09', 31, 2, '', 'EXPENSE'),
(730.00, 1, '2025-05-10', 32, 2, 'Перчини', 'EXPENSE'),
(480.00, 1, '2025-05-11', 33, 2, 'Бургер кинг', 'EXPENSE'),
(390.00, 1, '2025-05-12', 34, 2, 'Додо', 'EXPENSE'),
(2500.00, 9, '2025-05-13', 35, 2, 'Лента', 'EXPENSE'),
(350.00, 1, '2025-05-15', 36, 2, 'Додо', 'EXPENSE'),
(860.00, 1, '2025-05-16', 37, 2, 'Панини', 'EXPENSE'),
(320.00, 1, '2025-05-19', 38, 2, 'Жар-свежар', 'EXPENSE'),
(800.00, 5, '2025-05-14', 39, 2, 'Мастер класс по рисованию', 'EXPENSE'),
(120.00, 4, '2025-05-12', 40, 2, '', 'EXPENSE'),
(210.00, 4, '2025-05-13', 41, 2, '', 'EXPENSE'),
(60.00, 4, '2025-05-14', 42, 2, '', 'EXPENSE'),
(140.00, 4, '2025-05-17', 43, 2, '', 'EXPENSE'),
(6000.00, 3, '2025-05-12', 44, 2, 'Курсы по математике', 'EXPENSE'),
(290.00, 1, '2025-05-20', 45, 2, 'Шаурма', 'EXPENSE'),
(20000.00, 16, '2025-05-12', 46, 2, '', 'INCOME'),
(5000.00, 14, '2025-05-11', 48, 2, 'Вклад в цель: Поездка в Питер', 'EXPENSE'),
(20000.00, 17, '2025-05-05', 49, 2, 'Съём квартиры', 'EXPENSE'),
(10000.00, 16, '2025-05-08', 51, 2, '', 'INCOME'),
(2000.00, 13, '2025-05-05', 54, 2, 'Заём от Аня', 'INCOME'),
(1000.00, 13, '2025-05-19', 55, 2, 'Возврат долга Аня', 'EXPENSE'),
(1500.00, 13, '2025-05-01', 57, 2, 'Заём для Катя', 'EXPENSE'),
(3000.00, 13, '2025-05-12', 64, 2, 'Заём для Оля', 'EXPENSE'),
(3000.00, 13, '2025-05-21', 67, 2, 'Возврат долга от Оля', 'INCOME'),
(1000.00, 18, '2025-04-26', 68, 3, '', 'EXPENSE'),
(500.00, 1, '2025-05-17', 69, 3, 'Вкусно и точка', 'EXPENSE'),
(5000.00, 14, '2025-05-21', 71, 2, 'Вклад в цель: Поездка в Питер', 'EXPENSE'),
(1500.00, 13, '2025-05-22', 73, 3, 'Заём для Саша', 'EXPENSE'),
(3000.00, 19, '2025-05-22', 78, 3, 'Вклад в цель: День рождения', 'EXPENSE'),
(470.00, 27, '2025-05-22', 100, 2, '', 'EXPENSE'),
(5000.00, 3, '2025-05-23', 101, 2, 'Вклад в цель: Курсы по английскому', 'EXPENSE'),
(1500.00, 13, '2025-05-23', 102, 2, 'Заём от Саша', 'INCOME'),
(500.00, 13, '2025-05-23', 103, 2, 'Возврат долга Саша', 'EXPENSE'),
(1000.00, 13, '2025-05-23', 104, 2, 'Возврат долга Саша', 'EXPENSE'),
(230.00, 4, '2025-05-22', 105, 2, '', 'EXPENSE'),
(120.00, 4, '2025-05-25', 106, 2, '', 'EXPENSE'),
(1000.00, 13, '2025-05-26', 107, 2, 'Заём для Оля', 'EXPENSE');

-- --------------------------------------------------------

--
-- Структура таблицы `user`
--

CREATE TABLE `user` (
  `active` bit(1) NOT NULL,
  `id` int(11) NOT NULL,
  `last_activity` datetime(6) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- Дамп данных таблицы `user`
--

INSERT INTO `user` (`active`, `id`, `last_activity`, `email`, `password`, `username`) VALUES
(b'1', 1, '2025-05-26 17:39:29.000000', 'admin@example.com', '$2a$10$z2sEh7NJ4Z7Mp5QZi537S.0iNLvasiB7Qp1qk5OSjYyuDTh.THcGW', 'admin'),
(b'1', 2, '2025-05-26 17:39:04.000000', 'user@mail.ru', '$2a$10$e4uGn1Ma0FD/mF4JDlmX1uwOybWQVZQ952dsIWfqSeuk4AIGa2Pgu', 'user'),
(b'1', 3, '2025-05-22 23:03:58.000000', 'ponchik@mail.ru', '$2a$10$Ubx98EIbZA6notiRp.ED2OZ1kmBSIs76b6OwjHH8SafevJlMWnAQG', 'ponchik');

-- --------------------------------------------------------

--
-- Структура таблицы `user_roles`
--

CREATE TABLE `user_roles` (
  `user_id` int(11) NOT NULL,
  `roles` enum('ADMIN','USER') DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- Дамп данных таблицы `user_roles`
--

INSERT INTO `user_roles` (`user_id`, `roles`) VALUES
(1, 'ADMIN'),
(2, 'USER'),
(3, 'USER');

--
-- Индексы сохранённых таблиц
--

--
-- Индексы таблицы `category`
--
ALTER TABLE `category`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_category_name_user` (`name`,`user_id`),
  ADD KEY `FKpfk8djhv5natgshmxiav6xkpu` (`user_id`);

--
-- Индексы таблицы `debt`
--
ALTER TABLE `debt`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKk0myp61pfwjsai91qq7imxf4t` (`user_id`);

--
-- Индексы таблицы `goal`
--
ALTER TABLE `goal`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKiadq59lfc3x9h9jvxouon7niq` (`category_id`),
  ADD KEY `FKcxjbtjym9cn3ud0exw1qa39lm` (`user_id`);

--
-- Индексы таблицы `transaction`
--
ALTER TABLE `transaction`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKgik7ruym8r1n4xngrclc6kiih` (`category_id`),
  ADD KEY `FKsg7jp0aj6qipr50856wf6vbw1` (`user_id`);

--
-- Индексы таблицы `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UKob8kqyqqgmefl0aco34akdtpe` (`email`),
  ADD UNIQUE KEY `UKsb8bbouer5wak8vyiiy4pf2bx` (`username`);

--
-- Индексы таблицы `user_roles`
--
ALTER TABLE `user_roles`
  ADD KEY `FK55itppkw3i07do3h7qoclqd4k` (`user_id`);

--
-- AUTO_INCREMENT для сохранённых таблиц
--

--
-- AUTO_INCREMENT для таблицы `category`
--
ALTER TABLE `category`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=28;

--
-- AUTO_INCREMENT для таблицы `debt`
--
ALTER TABLE `debt`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- AUTO_INCREMENT для таблицы `goal`
--
ALTER TABLE `goal`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;

--
-- AUTO_INCREMENT для таблицы `transaction`
--
ALTER TABLE `transaction`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=108;

--
-- AUTO_INCREMENT для таблицы `user`
--
ALTER TABLE `user`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- Ограничения внешнего ключа сохраненных таблиц
--

--
-- Ограничения внешнего ключа таблицы `category`
--
ALTER TABLE `category`
  ADD CONSTRAINT `FKpfk8djhv5natgshmxiav6xkpu` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

--
-- Ограничения внешнего ключа таблицы `debt`
--
ALTER TABLE `debt`
  ADD CONSTRAINT `FKk0myp61pfwjsai91qq7imxf4t` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

--
-- Ограничения внешнего ключа таблицы `goal`
--
ALTER TABLE `goal`
  ADD CONSTRAINT `FKcxjbtjym9cn3ud0exw1qa39lm` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `FKiadq59lfc3x9h9jvxouon7niq` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`);

--
-- Ограничения внешнего ключа таблицы `transaction`
--
ALTER TABLE `transaction`
  ADD CONSTRAINT `FKgik7ruym8r1n4xngrclc6kiih` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`),
  ADD CONSTRAINT `FKsg7jp0aj6qipr50856wf6vbw1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

--
-- Ограничения внешнего ключа таблицы `user_roles`
--
ALTER TABLE `user_roles`
  ADD CONSTRAINT `FK55itppkw3i07do3h7qoclqd4k` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
