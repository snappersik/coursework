package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.annotations.AdminAction;
import com.almetpt.coursework.bookclub.mapper.CartMapper;
import com.almetpt.coursework.bookclub.model.Cart;
import com.almetpt.coursework.bookclub.service.CartService;
import com.almetpt.coursework.bookclub.service.userdetails.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/rest/carts")
@Tag(name = "Корзины", description = "Контроллер для работы с корзинами пользователей")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartMapper cartMapper;

    @Operation(summary = "Получить корзину по ID", description = "Позволяет получить корзину по её ID. Администраторы могут просматривать любые корзины, пользователи - только свои.")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/{id}")
    @AdminAction
    public ResponseEntity<?> getById(@PathVariable Long id, Authentication authentication) {
        // Проверяем, авторизован ли пользователь
        if (authentication == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Вы должны войти в систему для доступа к корзине");
        }

        // Проверяем, является ли пользователь администратором
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Если пользователь - администратор, разрешаем доступ к любой корзине
        if (isAdmin) {
            Optional<Cart> cartOptional = cartService.findById(id);
            if (cartOptional.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("Корзина с ID " + id + " не найдена");
            }
            return ResponseEntity.ok(cartMapper.toDTO(cartOptional.get()));
        } else {
            // Для обычного пользователя проверяем, что корзина принадлежит ему
            Long userId = null;
            try {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                userId = userDetails.getUserId().longValue();
            } catch (Exception e) {
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Не удалось определить ID пользователя");
            }

            Optional<Cart> cartOptional = cartService.findById(id);
            if (cartOptional.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("Корзина с ID " + id + " не найдена");
            }

            Cart cart = cartOptional.get();
            if (!cart.getUser().getId().equals(userId)) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("У вас нет доступа к этой корзине. Вы можете просматривать только свою корзину.");
            }

            return ResponseEntity.ok(cartMapper.toDTO(cart));
        }
    }

    @Operation(summary = "Добавить продукт в корзину", description = "Добавляет указанный продукт в корзину пользователя")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{userId}/products/{productId}")
    public ResponseEntity<?> addProductToCart(
            @Parameter(description = "ID пользователя") @PathVariable Long userId,
            @Parameter(description = "ID продукта") @PathVariable Long productId) {
        cartService.addProductToCart(userId, productId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Удалить продукт из корзины", description = "Удаляет указанный продукт из корзины пользователя")
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{userId}/products/{productId}")
    public ResponseEntity<?> removeProductFromCart(
            @Parameter(description = "ID пользователя") @PathVariable Long userId,
            @Parameter(description = "ID продукта") @PathVariable Long productId) {
        cartService.removeProductFromCart(userId, productId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить корзину текущего пользователя", description = "Возвращает корзину текущего аутентифицированного пользователя")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my-cart")
    public ResponseEntity<?> getMyCart(Authentication authentication) {
        // проверяем, авторизован ли пользователь
        if (authentication == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Вы должны войти в систему для доступа к корзине");
        }

        // если доступ пытается получить не пользователь
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOrganizer = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ORGANIZER"));

        if (isAdmin || isOrganizer) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("У вас нет корзины. Администраторы и организаторы не имеют личных корзин.");
        }

        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getUserId().longValue();

            Cart cart = cartService.getCartByUserId(userId);
            if (cart == null) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("Корзина не найдена");
            }

            return ResponseEntity.ok(cartMapper.toDTO(cart));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Не удалось получить корзину: " + e.getMessage());
        }
    }

}
