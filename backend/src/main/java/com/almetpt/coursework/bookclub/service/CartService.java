package com.almetpt.coursework.bookclub.service;

import com.almetpt.coursework.bookclub.dto.CartDTO;
import com.almetpt.coursework.bookclub.mapper.CartMapper;
import com.almetpt.coursework.bookclub.model.Cart;
import com.almetpt.coursework.bookclub.model.Product;
import com.almetpt.coursework.bookclub.model.User;
import com.almetpt.coursework.bookclub.repository.CartRepository;
import com.almetpt.coursework.bookclub.repository.ProductRepository;
import com.almetpt.coursework.bookclub.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.webjars.NotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList; // Импортируем ArrayList
import java.util.List;

@Service
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, CartMapper cartMapper, UserRepository userRepository,
            ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartMapper = cartMapper;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Cart saveCart(Cart cart) { // Сделаем публичным для OrderService
        return cartRepository.save(cart);
    }

    public CartDTO getCartById(Long id) {
        Cart cart = cartRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cart not found with id: " + id));
        return cartMapper.toDTO(cart);
    }

    // Не используется напрямую контроллером, но может быть полезен
    public CartDTO getCartByUser(User user) {
        return cartRepository.findByUser(user)
                .map(cartMapper::toDTO)
                .orElseGet(() -> cartMapper.toDTO(createCartForUser(user)));
    }

    @Transactional
    public Cart createCartForUser(User user) {
        log.info("Creating new cart for user id: {}", user.getId());
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setProducts(new ArrayList<>());
        cart.setCreatedWhen(LocalDateTime.now());
        cart.setCreatedBy("SYSTEM_USER_CREATION");
        return cartRepository.save(cart);
    }

    // Используется для добавления продукта через cartId (например, админом)
    @Transactional
    public CartDTO addProductToCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new NotFoundException("Cart not found with id: " + cartId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));

        List<Product> products = cart.getProducts();
        if (products == null) {
            products = new ArrayList<>(); // Инициализируем, если null
            cart.setProducts(products);
        }
        // Проверка, чтобы не добавлять дубликаты, если это не требуется
        if (products.stream().noneMatch(p -> p.getId().equals(productId))) {
            products.add(product);
        }
        cart.setUpdatedWhen(LocalDateTime.now());
        cart.setUpdatedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        return cartMapper.toDTO(cartRepository.save(cart));
    }

    // Используется для удаления продукта через cartId (например, админом)
    @Transactional
    public CartDTO removeProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new NotFoundException("Cart not found with id: " + cartId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));

        List<Product> products = cart.getProducts();
        if (products != null) {
            products.removeIf(p -> p.getId().equals(productId)); // Более безопасное удаление
        }
        cart.setUpdatedWhen(LocalDateTime.now());
        cart.setUpdatedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        return cartMapper.toDTO(cartRepository.save(cart));
    }

    @Transactional
    public CartDTO getCurrentUserCartDto() { // Переименован для ясности, что возвращает DTO
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findUserByEmailAndIsDeletedFalse(userEmail);
        if (user == null) {
            log.error("User not found with email: {}", userEmail);
            throw new NotFoundException("User not found with email: " + userEmail);
        }
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    log.warn("Cart not found for user {}, creating a new one.", userEmail);
                    return createCartForUser(user);
                });
        return cartMapper.toDTO(cart);
    }

    @Transactional(readOnly = true)
    public Cart getCurrentUserCartEntity() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findUserByEmailAndIsDeletedFalse(userEmail);
        if (user == null) {
            log.error("User not found with email: {}", userEmail);
            throw new NotFoundException("User not found with email: " + userEmail);
        }
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    log.warn("Cart entity not found for user {}, creating a new one for operation.", userEmail);
                    return createCartForUser(user);
                });
    }

    @Transactional
    public CartDTO addProductToCurrentUserCart(Long productId) {
        Cart cart = getCurrentUserCartEntity(); // Этот метод теперь может создать корзину
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));

        List<Product> products = cart.getProducts();
        if (products == null) {
            products = new ArrayList<>();
            cart.setProducts(products);
        }
        if (products.stream().noneMatch(p -> p.getId().equals(productId))) {
            products.add(product);
        }
        cart.setUpdatedWhen(LocalDateTime.now());
        cart.setUpdatedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        return cartMapper.toDTO(cartRepository.save(cart));
    }

    @Transactional
    public CartDTO removeProductFromCurrentUserCart(Long productId) {
        Cart cart = getCurrentUserCartEntity(); // Этот метод теперь может создать корзину
        // Product product = productRepository.findById(productId) // Не обязательно
        // загружать, если удаляем по ID
        // .orElseThrow(() -> new NotFoundException("Product not found with id: " +
        // productId));

        List<Product> products = cart.getProducts();
        if (products != null) {
            products.removeIf(p -> p.getId().equals(productId));
        }
        cart.setUpdatedWhen(LocalDateTime.now());
        cart.setUpdatedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        return cartMapper.toDTO(cartRepository.save(cart));
    }

    @Transactional
    public CartDTO clearCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new NotFoundException("Cart not found with id: " + cartId));

        // Проверка прав доступа - только владелец корзины или админ может ее чистить
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!cart.getUser().getEmail().equals(currentUserEmail) && !isAdmin) {
            log.warn("User {} attempted to clear cart {} belonging to {}", currentUserEmail, cartId,
                    cart.getUser().getEmail());
            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not have permission to clear this cart.");
        }

        if (cart.getProducts() != null) {
            cart.getProducts().clear();
        }
        cart.setUpdatedWhen(LocalDateTime.now());
        cart.setUpdatedBy(currentUserEmail);
        return cartMapper.toDTO(cartRepository.save(cart));
    }

    @Transactional
    public CartDTO clearCurrentUserCart() {
        Cart cart = getCurrentUserCartEntity();
        if (cart.getProducts() != null) {
            cart.getProducts().clear();
        }
        cart.setUpdatedWhen(LocalDateTime.now());
        cart.setUpdatedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        return cartMapper.toDTO(cartRepository.save(cart));
    }
}
