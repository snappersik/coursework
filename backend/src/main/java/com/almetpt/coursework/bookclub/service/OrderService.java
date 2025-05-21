package com.almetpt.coursework.bookclub.service;

import com.almetpt.coursework.bookclub.dto.CartDTO;
import com.almetpt.coursework.bookclub.dto.OrderDTO;
import com.almetpt.coursework.bookclub.mapper.OrderMapper;
import com.almetpt.coursework.bookclub.model.*;
import com.almetpt.coursework.bookclub.repository.OrderRepository;
import com.almetpt.coursework.bookclub.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.webjars.NotFoundException;

import java.io.File;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class OrderService extends GenericService<Order, OrderDTO> {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CartService cartService;
    private final UserRepository userRepository;
    
    @Autowired
    private JavaMailSender emailSender;

    public OrderService(OrderRepository orderRepository, OrderMapper orderMapper, CartService cartService,
                      UserRepository userRepository, JavaMailSender emailSender) {
        super(orderRepository, orderMapper);
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.cartService = cartService;
        this.userRepository = userRepository;
        this.emailSender = emailSender;
    }

    public Page<OrderDTO> getOrdersByUser(Pageable pageable) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findUserByEmailAndIsDeletedFalse(userEmail);
        if (user == null) {
            throw new NotFoundException("User not found with email: " + userEmail);
        }

        Page<Order> orders = orderRepository.findByUser(user, pageable);
        return orders.map(orderMapper::toDTO);
    }

    public List<OrderDTO> getOrdersByUser() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findUserByEmailAndIsDeletedFalse(userEmail);
        if (user == null) {
            throw new NotFoundException("User not found with email: " + userEmail);
        }

        List<Order> orders = orderRepository.findByUser(user);
        return orderMapper.toDTOs(orders);
    }

    @Transactional
    public OrderDTO createOrderFromCart() {
        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findUserByEmailAndIsDeletedFalse(userEmail);
            if (user == null) {
                throw new NotFoundException("User not found with email: " + userEmail);
            }

            CartDTO cartDTO = cartService.getCurrentUserCart();
            Cart cart = cartService.getCartEntity(cartDTO.getId());
            if (cart.getProducts() == null || cart.getProducts().isEmpty()) {
                throw new IllegalStateException("Cart is empty, cannot create order.");
            }

            Order order = new Order();
            order.setUser(user);
            order.setStatus(OrderStatus.PENDING);
            order.setProducts(cart.getProducts());
            BigDecimal total = cart.getProducts().stream()
                .map(Product::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            order.setTotal(total);
            setAuditFields(order, true);
            Order savedOrder = orderRepository.save(order);
            
            // Clear the cart after creating the order
            cart.setProducts(null);
            cartService.saveCart(cart);
            
            try {
                sendOrderConfirmationEmail(savedOrder);
            } catch (MessagingException e) {
                log.error("Failed to send order confirmation email", e);
                // Не прерываем выполнение метода из-за ошибки отправки письма
            }
            
            return orderMapper.toDTO(savedOrder);
        } catch (Exception e) {
            log.error("Error creating order from cart", e);
            throw e;
        }
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));
        order.setStatus(OrderStatus.valueOf(status));
        setAuditFields(order, false);
        return orderMapper.toDTO(orderRepository.save(order));
    }

    private void sendOrderConfirmationEmail(Order order) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom("spring.project.42@mail.ru");
        helper.setTo(order.getUser().getEmail());
        helper.setSubject("Подтверждение заказа #" + order.getId() + " (Подробное)");
        
        StringBuilder messageText = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        
        messageText.append("Номер заказа: ").append(order.getId()).append("\n");
        messageText.append("Дата заказа: ").append(order.getCreatedWhen().format(formatter)).append("\n");
        messageText.append("Общая сумма: ").append(order.getTotal()).append("\n");
        messageText.append("Состав заказа:\n");
        
        for (Product product : order.getProducts()) {
            messageText.append("- ").append(product.getName()).append(" (").append(product.getPrice()).append(")\n");
        }
        
        helper.setText(messageText.toString(), false);
        
        boolean hasElectronicProducts = false;
        
        try {
            for (Product product : order.getProducts()) {
                if (product.hasElectronicFile()) {
                    hasElectronicProducts = true;
                    File file = new File(product.getElectronicProductFilename());
                    if (file.exists()) {
                        FileSystemResource fileResource = new FileSystemResource(file);
                        helper.addAttachment(product.getOriginalElectronicProductFilename(), fileResource);
                    } else {
                        log.warn("Electronic file not found: {}", product.getElectronicProductFilename());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error attaching electronic files to email", e);
        }
        
        if (!hasElectronicProducts) {
            messageText.append("\nВ вашем заказе нет электронных товаров для прикрепления.");
        }
        
        helper.setText(messageText.toString(), false);
        emailSender.send(message);
    }
}
