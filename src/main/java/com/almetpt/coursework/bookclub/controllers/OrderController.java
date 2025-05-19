package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.annotations.AdminAction;
import com.almetpt.coursework.bookclub.dto.OrderDTO;
import com.almetpt.coursework.bookclub.model.Order;
import com.almetpt.coursework.bookclub.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rest/orders")
@Tag(name = "Заказы", description = "Контроллер для работы с заказами")
public class OrderController extends GenericController<Order, OrderDTO> {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        super(orderService);
        this.orderService = orderService;
    }

    @Operation(summary = "Получить страницу заказов", description = "Возвращает страницу заказов с пагинацией")
    @GetMapping("/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<Page<OrderDTO>> getOrdersPaginated(
            @PageableDefault(size = 20, sort = "createdWhen", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<OrderDTO> orders = orderService.listAll(pageable);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Получить заказы текущего пользователя", description = "Возвращает список заказов, созданных текущим пользователем")
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrderDTO>> getMyOrders() {
        return ResponseEntity.ok(orderService.getMyOrders());
    }

    @Operation(summary = "Создать заказ из корзины", description = "Создает новый заказ на основе текущей корзины пользователя")
    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderDTO> createOrderFromCart() {
        return ResponseEntity.ok(orderService.createOrderFromCart());
    }

    @Operation(summary = "Изменить статус заказа", description = "Позволяет администратору изменить статус заказа")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @AdminAction
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @Parameter(description = "ID заказа") @PathVariable Long id,
            @Parameter(description = "Новый статус заказа") @RequestParam String status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }
}
