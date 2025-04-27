package com.almetpt.coursework.bookclub.service;

import com.almetpt.coursework.bookclub.model.Cart;
import com.almetpt.coursework.bookclub.model.Product;
import com.almetpt.coursework.bookclub.model.User;
import com.almetpt.coursework.bookclub.repository.CartRepository;
import com.almetpt.coursework.bookclub.repository.ProductRepository;
import com.almetpt.coursework.bookclub.utils.MailUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Transactional(readOnly = true)
    public Cart getCartByUserId(Long userId) {
        Cart cart = cartRepository.findByUserId(userId);
        // Принудительная инициализация коллекции продуктов
        if (cart != null && cart.getProducts() != null) {
            cart.getProducts().size();
        }
        return cart;
    }

    @Transactional
    public Cart createCartForUser(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setCreatedBy("AUTO");
        cart.setCreatedWhen(LocalDateTime.now());
        cart.setDeleted(false);
        return cartRepository.save(cart);
    }

    @Transactional
    public void addProductToCart(Long userId, Long productId) {
        Cart cart = getCartByUserId(userId);
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
        if (!cart.getProducts().contains(product)) {
            cart.getProducts().add(product);
            cartRepository.save(cart);
        }
    }

    @Transactional
    public void removeProductFromCart(Long userId, Long productId) {
        Cart cart = getCartByUserId(userId);
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
        if (cart.getProducts().contains(product)) {
            cart.getProducts().remove(product);
            cartRepository.save(cart);
        }
    }

    @Transactional
    public void purchaseProduct(Long userId, Long productId, String userEmail) {
        Cart cart = getCartByUserId(userId);
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
        if (cart.getProducts().contains(product)) {
            // Удаляем товар из корзины
            cart.getProducts().remove(product);
            cartRepository.save(cart);
            // Отправляем подтверждение на email
            sendOrderConfirmationEmail(userEmail, product);
        } else {
            throw new RuntimeException("Product is not in the cart");
        }
    }

    private void sendOrderConfirmationEmail(String userEmail, Product product) {
        String subject = "Подтверждение заказа";
        String text = "Спасибо за покупку!\n\n" +
                "Детали заказа:\n" +
                "Название: " + product.getName();
        mailSender.send(MailUtils.createMailMessage(userEmail, subject, text));
    }

    public Optional<Cart> findById(Long id) {
        return cartRepository.findById(id);
    }
}
