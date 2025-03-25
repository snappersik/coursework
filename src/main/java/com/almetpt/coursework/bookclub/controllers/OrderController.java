package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.dto.OrderDTO;
import com.almetpt.coursework.bookclub.model.Order;
import com.almetpt.coursework.bookclub.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rest/orders")
@Tag(name = "Заказы", description = "Контроллер для работы с заказами")
public class OrderController extends GenericController<Order, OrderDTO> {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        super(orderService);
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderDTO orderDTO) {
        return ResponseEntity.ok(orderService.create(orderDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOne(id));
    }
}
