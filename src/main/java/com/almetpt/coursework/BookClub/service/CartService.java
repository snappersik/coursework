package com.almetpt.coursework.BookClub.service;

import com.almetpt.coursework.BookClub.model.Cart;
import com.almetpt.coursework.BookClub.model.Product;
import com.almetpt.coursework.BookClub.repository.CartRepository;
import com.almetpt.coursework.BookClub.repository.ProductRepository;
import com.almetpt.coursework.BookClub.utils.MailUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JavaMailSender mailSender;

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
}
