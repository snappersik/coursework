package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.dto.CartDTO;
import com.almetpt.coursework.bookclub.service.CartService;
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
    public ResponseEntity<CartDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(cartService.getCartById(id));
    }

    @PostMapping("/{cartId}/add/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartDTO> addProductToCart(@PathVariable Long cartId, @PathVariable Long productId) {
        return ResponseEntity.ok(cartService.addProductToCart(cartId, productId));
    }

    @DeleteMapping("/{cartId}/remove/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartDTO> removeProductFromCart(@PathVariable Long cartId, @PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeProductFromCart(cartId, productId));
    }

    @GetMapping("/my-cart")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartDTO> getCurrentUserCart() {
        return ResponseEntity.ok(cartService.getCurrentUserCart());
    }
}
