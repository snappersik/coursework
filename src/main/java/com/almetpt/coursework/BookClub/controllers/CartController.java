package com.almetpt.coursework.BookClub.controller;

import com.almetpt.coursework.BookClub.dto.CartDTO;
import com.almetpt.coursework.BookClub.mapper.CartMapper;
import com.almetpt.coursework.BookClub.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartMapper cartMapper;

    @GetMapping("/{userId}")
    public ResponseEntity<CartDTO> getCartByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(cartMapper.toDTO(cartService.getCartByUserId(userId)));
    }

    @PostMapping("/{userId}/add/{productId}")
    public ResponseEntity<Void> addProductToCart(@PathVariable Long userId, @PathVariable Long productId) {
        cartService.addProductToCart(userId, productId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/remove/{productId}")
    public ResponseEntity<Void> removeProductFromCart(@PathVariable Long userId, @PathVariable Long productId) {
        cartService.removeProductFromCart(userId, productId);
        return ResponseEntity.ok().build();
    }
}
