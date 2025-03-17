package com.almetpt.coursework.BookClub.service;

import com.almetpt.coursework.BookClub.model.Cart;
import com.almetpt.coursework.BookClub.model.Product;
import com.almetpt.coursework.BookClub.repository.CartRepository;
import com.almetpt.coursework.BookClub.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    public void addProductToCart(Long userId, Long productId) {
        Cart cart = getCartByUserId(userId);
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
        if (!cart.getProducts().contains(product)) {
            cart.getProducts().add(product);
            cartRepository.save(cart);
        }
    }

    public void removeProductFromCart(Long userId, Long productId) {
        Cart cart = getCartByUserId(userId);
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
        if (cart.getProducts().contains(product)) {
            cart.getProducts().remove(product);
            cartRepository.save(cart);
        }
    }
}
