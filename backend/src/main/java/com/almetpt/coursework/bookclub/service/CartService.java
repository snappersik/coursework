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
    public Cart saveCart(Cart cart) {
        return cartRepository.save(cart);
    }

    public CartDTO getCartById(Long id) {
        Cart cart = cartRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cart not found with id: " + id));
        return cartMapper.toDTO(cart);
    }

    public CartDTO getCartByUser(User user) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("Cart not found for user: " + user.getEmail()));
        return cartMapper.toDTO(cart);
    }

    @Transactional
    public Cart createCartForUser(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setCreatedWhen(LocalDateTime.now());
        cart.setCreatedBy("SYSTEM");
        return cartRepository.save(cart);
    }

    @Transactional
    public CartDTO addProductToCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new NotFoundException("Cart not found with id: " + cartId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));

        List<Product> products = cart.getProducts();
        if (products == null) {
            cart.setProducts(List.of(product));
        } else {
            products.add(product);
        }
        return cartMapper.toDTO(cartRepository.save(cart));
    }

    @Transactional
    public CartDTO removeProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new NotFoundException("Cart not found with id: " + cartId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));

        List<Product> products = cart.getProducts();
        if (products != null) {
            products.remove(product);
            cart.setProducts(products);
        }
        return cartMapper.toDTO(cartRepository.save(cart));
    }

    public CartDTO getCurrentUserCart() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findUserByEmailAndIsDeletedFalse(userEmail);
        if (user == null) {
            throw new NotFoundException("User not found with email: " + userEmail);
        }
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("Cart not found for user: " + userEmail));
        return cartMapper.toDTO(cart);
    }

    public Cart getCartEntity(Long cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new NotFoundException("Cart not found with id: " + cartId));
    }
}
