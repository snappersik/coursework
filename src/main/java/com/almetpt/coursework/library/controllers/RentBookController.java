package com.almetpt.coursework.library.controllers;


import com.almetpt.coursework.library.dto.BookRentInfoDTO;
import com.almetpt.coursework.library.model.Order;
import com.almetpt.coursework.library.repository.CartRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rest/rent/info")
@Tag(name = "Аренда книг",
        description = "Контроллер для работы с арендой/выдачей книг пользователем библиотеки")
public class RentBookController
        extends GenericController<Order, BookRentInfoDTO>{

    public RentBookController(CartRepository genericRepository, CartRepository bookRentInfoRepository, BookRentInfoService bookRentInfoService) {
        super(bookRentInfoService);
    }
}