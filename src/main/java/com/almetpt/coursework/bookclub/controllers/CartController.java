package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.dto.CartDTO;
import com.almetpt.coursework.bookclub.mapper.CartMapper;
import com.almetpt.coursework.bookclub.model.Cart;
import com.almetpt.coursework.bookclub.service.CartService;
import com.almetpt.coursework.bookclub.service.userdetails.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/carts")
@Tag(name = "Корзины", description = "Контроллер для работы с корзинами пользователей")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartMapper cartMapper;

    @Operation(description = "Получить корзину пользователя")
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id, Authentication authentication) {
        // Проверяем, авторизован ли пользователь
        if (authentication == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Вы должны войти в систему для доступа к корзине");
        }

        // Получаем ID текущего пользователя с проверкой на null
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Integer userIdInteger = userDetails.getUserId();
        Long userId = (userIdInteger != null) ? userIdInteger.longValue() : null;

        // Если ID пользователя null, возвращаем ошибку
        if (userId == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Не удалось определить ID пользователя");
        }

        // Получаем корзину
        Optional<Cart> cartOptional = cartService.findById(id);

        // Проверяем, существует ли корзина
        if (cartOptional.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Корзина с ID " + id + " не найдена");
        }

        Cart cart = cartOptional.get();

        // Проверяем, принадлежит ли корзина текущему пользователю
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!cart.getUser().getId().equals(userId) && !isAdmin) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("У вас нет доступа к этой корзине. Вы можете просматривать только свою корзину.");
        }

        // Если все проверки пройдены, возвращаем корзину
        return ResponseEntity.ok(cartMapper.toDTO(cart));
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
