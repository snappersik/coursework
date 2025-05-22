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
import org.springframework.core.io.Resource; // Изменен импорт
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.webjars.NotFoundException;

// import java.io.File; // Больше не нужен для FileSystemResource напрямую
import java.math.BigDecimal;
// import java.time.LocalDateTime; // Не используется напрямую здесь
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OrderService extends GenericService<Order, OrderDTO> {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CartService cartService;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService; // Добавлено

    @Autowired
    private JavaMailSender emailSender;

    public OrderService(OrderRepository orderRepository, OrderMapper orderMapper, CartService cartService,
            UserRepository userRepository, JavaMailSender emailSender, FileStorageService fileStorageService) { // Добавлено
        super(orderRepository, orderMapper);
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.cartService = cartService;
        this.userRepository = userRepository;
        this.emailSender = emailSender;
        this.fileStorageService = fileStorageService; // Добавлено
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
    public OrderDTO createOrderFromCart() { // MessagingException убрано, т.к. ошибка почты не должна откатывать транзакцию
        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findUserByEmailAndIsDeletedFalse(userEmail);
            if (user == null) {
                throw new NotFoundException("User not found with email: " + userEmail);
            }

            CartDTO cartDTO = cartService.getCurrentUserCartDto();
            // Cart cart = cartService.getCartEntity(cartDTO.getId()); // Старый вариант
            Cart cart = cartService.getCurrentUserCartEntity(); // Получаем сущность корзины текущего пользователя

            if (cart.getProducts() == null || cart.getProducts().isEmpty()) {
                throw new IllegalStateException("Cart is empty, cannot create order.");
            }

            Order order = new Order();
            order.setUser(user);
            order.setStatus(OrderStatus.PENDING);
            order.setProducts(new ArrayList<>(cart.getProducts())); // Копируем список, чтобы избежать проблем с Hibernate
            BigDecimal total = cart.getProducts().stream()
                    .map(Product::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            order.setTotal(total);
            setAuditFields(order, true); // from GenericService
            Order savedOrder = orderRepository.save(order);

            // Clear the cart after creating the order
            cart.getProducts().clear(); // Очищаем список продуктов в сущности корзины
            cartService.saveCart(cart); // Сохраняем изменения в корзине (теперь она пуста)

            try {
                sendOrderConfirmationEmail(savedOrder);
            } catch (MessagingException e) {
                log.error("Failed to send order confirmation email for order {}: {}", savedOrder.getId(), e.getMessage());
                // Не прерываем выполнение метода из-за ошибки отправки письма, заказ уже создан
            }

            return orderMapper.toDTO(savedOrder);
        } catch (Exception e) {
            log.error("Error creating order from cart", e);
            throw e; // Перебрасываем исключение для отката транзакции, если это критическая ошибка
        }
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));
        try {
            order.setStatus(OrderStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid order status: {}", status);
            throw new IllegalArgumentException("Invalid order status: " + status);
        }
        setAuditFields(order, false); // from GenericService
        return orderMapper.toDTO(orderRepository.save(order));
    }

    @SuppressWarnings("null")
    private void sendOrderConfirmationEmail(Order order) throws MessagingException {
        if (order == null || order.getUser() == null || order.getUser().getEmail() == null) {
            log.error("Cannot send email: order, user, or email is undefined for order ID (if available): {}", order != null ? order.getId() : "N/A");
            return;
        }

        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("spring.project.42@mail.ru"); // Убедитесь, что этот email может отправлять почту
            helper.setTo(order.getUser().getEmail());
            helper.setSubject("Подтверждение заказа #" + order.getId());

            StringBuilder messageText = new StringBuilder();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            messageText.append("Благодарим вас за заказ в Книжной Гавани!");
            messageText.append("\n\n");
            messageText.append("Номер заказа: #").append(order.getId()).append("\n");
            messageText.append("Дата заказа: ").append(order.getCreatedWhen().format(formatter)).append("\n");
            messageText.append("Общая сумма: ").append(order.getTotal()).append(" ₽\n\n");
            messageText.append("Состав заказа:\n");

            if (order.getProducts() != null && !order.getProducts().isEmpty()) {
                for (Product product : order.getProducts()) {
                    if (product != null) {
                        String productName = product.getName() != null ? product.getName() : "Неизвестный товар";
                        String productPrice = product.getPrice() != null ? product.getPrice().toString() : "0.00";
                        String productCategory = product.getCategory() != null ? product.getCategory().getDescription() : ""; // Используем описание

                        messageText.append("- ").append(productName)
                                .append(" (").append(productCategory).append("): ")
                                .append(productPrice).append(" ₽\n");
                    }
                }
            } else {
                messageText.append("- Товары не указаны\n");
            }
            
            messageText.append("\n"); // Добавляем пустую строку перед секцией вложений

            boolean hasElectronicProducts = false;
            if (order.getProducts() != null) {
                for (Product product : order.getProducts()) {
                    // Проверяем, есть ли у продукта электронный файл и имя файла
                    if (product != null && product.hasElectronicFile() && product.getElectronicProductFilename() != null && !product.getElectronicProductFilename().isEmpty()) {
                        try {
                            Resource resource = fileStorageService.loadAsResource(product.getElectronicProductFilename());
                            if (resource.exists() && resource.isReadable()) {
                                String attachmentName = product.getOriginalElectronicProductFilename() != null
                                        ? product.getOriginalElectronicProductFilename()
                                        : resource.getFilename(); // Используем имя файла из ресурса, если оригинальное недоступно
                                helper.addAttachment(attachmentName, resource);
                                hasElectronicProducts = true;
                                log.info("Attachment added: {} for order {}", attachmentName, order.getId());
                            } else {
                                log.warn("Electronic file resource not found or not readable: {} for product ID {}", product.getElectronicProductFilename(), product.getId());
                            }
                        } catch (Exception e) {
                            log.error("Error attaching electronic file {} for product ID {}: {}", product.getElectronicProductFilename(), product.getId(), e.getMessage());
                        }
                    }
                }
            }

            if (hasElectronicProducts) {
                 messageText.append("Ваши электронные товары прикреплены к этому письму.\n");
            } else {
                messageText.append("В вашем заказе нет электронных товаров для прикрепления.\n");
            }

            helper.setText(messageText.toString(), false); // Устанавливаем текст письма

            emailSender.send(message);
            log.info("Order confirmation email #{} sent successfully to {}", order.getId(), order.getUser().getEmail());
        } catch (MessagingException e) {
            log.error("MessagingException while sending order confirmation for order #{}: {}", order.getId(), e.getMessage(), e);
            throw e; // Перебрасываем, чтобы вызывающий метод знал об ошибке
        } catch (Exception e) {
            log.error("Unexpected error while sending order confirmation for order #{}: {}", order.getId(), e.getMessage(), e);
            // Не перебрасываем другие исключения, чтобы не откатывать основную транзакцию
        }
    }
}
