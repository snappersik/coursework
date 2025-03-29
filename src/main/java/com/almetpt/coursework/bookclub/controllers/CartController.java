package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.dto.CartDTO;
import com.almetpt.coursework.bookclub.mapper.CartMapper;
import com.almetpt.coursework.bookclub.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@Tag(name = "Корзины", description = "Контроллер для работы с корзинами пользователей")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartMapper cartMapper;

    @Operation(description = "Получить корзину пользователя")
    @GetMapping("/{userId}")
    public ResponseEntity<CartDTO> getCartByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(cartMapper.toDTO(cartService.getCartByUserId(userId)));
    }

    @Operation(description = "Добавить товар в корзину пользователя")
    @PostMapping("/{userId}/products/{productId}")
    public ResponseEntity<Void> addProductToCart(@PathVariable Long userId, @PathVariable Long productId) {
        cartService.addProductToCart(userId, productId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(description = "Удалить товар из корзины пользователя")
    @DeleteMapping("/{userId}/products/{productId}")
    public ResponseEntity<Void> removeProductFromCart(@PathVariable Long userId, @PathVariable Long productId) {
        cartService.removeProductFromCart(userId, productId);
        return ResponseEntity.noContent().build();
    }
}
