package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.dto.CartDTO;
import com.almetpt.coursework.bookclub.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rest/carts")
@Slf4j
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @cartSecurityService.isOwner(authentication, #id)") // Пример кастомной проверки
    @Operation(summary = "Get cart by ID (Admin or Owner)")
    public ResponseEntity<CartDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(cartService.getCartById(id));
    }

    // Эндпоинт для добавления продукта В КОРЗИНУ ТЕКУЩЕГО ПОЛЬЗОВАТЕЛЯ
    @PostMapping("/products/{productId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add product to current user's cart")
    public ResponseEntity<CartDTO> addProductToCurrentUserCart(@PathVariable Long productId) {
        return ResponseEntity.ok(cartService.addProductToCurrentUserCart(productId));
    }

    // Эндпоинт для удаления продукта ИЗ КОРЗИНЫ ТЕКУЩЕГО ПОЛЬЗОВАТЕЛЯ
    @DeleteMapping("/products/{productId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove product from current user's cart")
    public ResponseEntity<CartDTO> removeProductFromCurrentUserCart(@PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeProductFromCurrentUserCart(productId));
    }
    
    // Эндпоинты для администрирования specific cart (если нужно)
    @PostMapping("/{cartId}/admin/products/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ADMIN: Add product to a specific cart")
    public ResponseEntity<CartDTO> adminAddProductToCart(@PathVariable Long cartId, @PathVariable Long productId) {
        return ResponseEntity.ok(cartService.addProductToCart(cartId, productId));
    }

    @DeleteMapping("/{cartId}/admin/products/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ADMIN: Remove product from a specific cart")
    public ResponseEntity<CartDTO> adminRemoveProductFromCart(@PathVariable Long cartId, @PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeProductFromCart(cartId, productId));
    }


    @GetMapping("/my-cart")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user's cart")
    public ResponseEntity<CartDTO> getCurrentUserCart() {
        return ResponseEntity.ok(cartService.getCurrentUserCartDto());
    }

    // Очистка корзины по ID (например, для админа)
    @PutMapping("/{cartId}/clear")
    @PreAuthorize("hasRole('ADMIN') or @cartSecurityService.isOwner(authentication, #cartId)")
    @Operation(summary = "Clear a specific cart (Admin or Owner)")
    public ResponseEntity<CartDTO> clearCart(@PathVariable Long cartId) {
        return ResponseEntity.ok(cartService.clearCart(cartId));
    }

    // Очистка корзины текущего пользователя
    @PutMapping("/my-cart/clear")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Clear current user's cart")
    public ResponseEntity<CartDTO> clearCurrentUserCart() {
        return ResponseEntity.ok(cartService.clearCurrentUserCart());
    }
}
