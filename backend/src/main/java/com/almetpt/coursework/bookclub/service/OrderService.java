package com.almetpt.coursework.bookclub.service;

import com.almetpt.coursework.bookclub.constants.Errors;
import com.almetpt.coursework.bookclub.dto.OrderDTO;
import com.almetpt.coursework.bookclub.mapper.OrderMapper;
import com.almetpt.coursework.bookclub.model.Cart;
import com.almetpt.coursework.bookclub.model.Order;
import com.almetpt.coursework.bookclub.model.OrderStatus;
import com.almetpt.coursework.bookclub.model.Product;
import com.almetpt.coursework.bookclub.model.User;
import com.almetpt.coursework.bookclub.repository.CartRepository;
import com.almetpt.coursework.bookclub.repository.OrderRepository;
import com.almetpt.coursework.bookclub.repository.UserRepository;

import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.Resource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.webjars.NotFoundException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderService extends GenericService<Order, OrderDTO> {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartService cartService;
    private final JavaMailSender emailSender;
    private final FileStorageService fileStorageService;

    public OrderService(OrderRepository orderRepository,
            OrderMapper orderMapper,
            UserRepository userRepository,
            CartRepository cartRepository,
            CartService cartService,
            JavaMailSender emailSender,
            FileStorageService fileStorageService) {
        super(orderRepository, orderMapper);
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.cartService = cartService;
        this.emailSender = emailSender;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public OrderDTO createOrder(OrderDTO orderDTO) {
        Order order = orderMapper.toEntity(orderDTO);
        order = orderRepository.save(order);
        return orderMapper.toDTO(order);
    }

    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return orderMapper.toDTO(order);
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getMyOrders() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findUserByEmailAndIsDeletedFalse(email);
        if (currentUser == null) {
            throw new NotFoundException("Текущий пользователь не найден");
        }

        Page<Order> orders = orderRepository.findByUserId(currentUser.getId(), Pageable.unpaged());
        return orderMapper.toDTOs(orders.getContent());
    }

    public void sendOrderConfirmationEmail(Order order) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("spring.project.42@mail.ru");
            helper.setTo(order.getUser().getEmail());
            helper.setSubject("Подтверждение заказа #" + order.getId() + " (Подробное)");

            StringBuilder messageText = new StringBuilder();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            messageText.append("<html><body>");
            messageText.append("<h2>Уважаемый(ая) ").append(order.getUser().getFirstName()).append(",</h2>");
            messageText.append("<p>Благодарим вас за заказ в Книжной Гавани!</p>");
            messageText.append("<p><b>Номер заказа:</b> #").append(order.getId()).append("</p>");
            messageText.append("<p><b>Дата заказа:</b> ").append(order.getCreatedWhen().format(formatter))
                    .append("</p>");
            messageText.append("<p><b>Общая сумма:</b> ").append(order.getTotal()).append(" ₽</p>");

            messageText.append("<h3>Состав заказа:</h3>");
            messageText.append("<ul>");
            for (Product product : order.getProducts()) {
                messageText.append("<li>").append(product.getName())
                        .append(" (").append(product.getCategory().getDescription()).append("): ")
                        .append(product.getPrice()).append(" ₽</li>");
            }
            messageText.append("</ul>");

            boolean hasElectronicProducts = order.getProducts().stream().anyMatch(Product::hasElectronicFile);
            if (hasElectronicProducts) {
                messageText.append("<p>Ваши электронные товары (если есть) прикреплены к этому письму.</p>");
            } else {
                messageText.append("<p>В вашем заказе нет электронных товаров для прикрепления.</p>");
            }

            messageText.append("<p>С уважением,<br>Команда Книжной Гавани</p>");
            messageText.append("</body></html>");
            helper.setText(messageText.toString(), true);

            for (Product product : order.getProducts()) {
                if (product.hasElectronicFile()) {
                    try {
                        Resource resource = fileStorageService.loadAsResource(product.getElectronicProductFilename());
                        if (resource.exists() && resource.isReadable()) {
                            helper.addAttachment(product.getOriginalElectronicProductFilename(), resource);
                            log.info("Файл {} успешно прикреплен к HTML письму для заказа #{}",
                                    product.getOriginalElectronicProductFilename(), order.getId());
                        } else {
                            log.warn("Не удалось прочитать или найти файл {} для прикрепления к HTML заказу #{}",
                                    product.getElectronicProductFilename(), order.getId());
                        }
                    } catch (Exception e) {
                        log.error("Ошибка при прикреплении файла {} к HTML письму для заказа #{}: {}",
                                product.getOriginalElectronicProductFilename(), order.getId(), e.getMessage());
                    }
                }
            }
            emailSender.send(message);
            log.info("HTML подтверждение заказа #{} с вложениями успешно отправлено на email {}.", order.getId(),
                    order.getUser().getEmail());
        } catch (Exception e) {
            log.error(
                    "Ошибка отправки HTML письма с подтверждением заказа #{} и вложениями: {}. Попытка отправки упрощенного письма.",
                    order.getId(), e.getMessage(), e);
            sendSimpleOrderConfirmation(order);
        }
    }

    @Transactional
    public OrderDTO createOrderFromCart() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findUserByEmailAndIsDeletedFalse(email);
        if (currentUser == null) {
            throw new NotFoundException("Текущий пользователь не найден");
        }

        Cart cart = cartService.getCartByUserId(currentUser.getId());
        if (cart == null) {
            log.warn("Корзина не найдена для пользователя {}", currentUser.getId());
            throw new IllegalStateException(
                    "Корзина не найдена для пользователя. Пожалуйста, добавьте товары в корзину.");
        }

        if (cart.getProducts() == null || cart.getProducts().isEmpty()) {
            throw new IllegalStateException("Корзина пуста. Невозможно создать заказ.");
        }

        Order order = new Order();
        order.setUser(currentUser);
        order.setProducts(new ArrayList<>(cart.getProducts()));
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedBy(email);
        order.setCreatedWhen(LocalDateTime.now());

        BigDecimal total = cart.getProducts().stream()
                .map(Product::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotal(total);

        Order savedOrder = orderRepository.save(order);

        sendOrderConfirmationEmail(savedOrder);

        cart.getProducts().clear();
        cartRepository.save(cart);

        return orderMapper.toDTO(savedOrder);
    }

    public void sendOrderConfirmationWithAttachments(Order order) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("spring.project.42@mail.ru");
            helper.setTo(order.getUser().getEmail());
            helper.setSubject("Подтверждение заказа #" + order.getId() + " (Подробное)");

            StringBuilder messageText = new StringBuilder();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            messageText.append("<html><body>");
            messageText.append("<h2>Уважаемый(ая) ").append(order.getUser().getFirstName()).append(",</h2>");
            messageText.append("<p>Благодарим вас за заказ в Книжной Гавани!</p>");
            messageText.append("<p><b>Номер заказа:</b> #").append(order.getId()).append("</p>");
            messageText.append("<p><b>Дата заказа:</b> ").append(order.getCreatedWhen().format(formatter))
                    .append("</p>");
            messageText.append("<p><b>Общая сумма:</b> ").append(order.getTotal()).append(" ₽</p>");

            messageText.append("<h3>Состав заказа:</h3>");
            messageText.append("<ul>");
            for (Product product : order.getProducts()) {
                messageText.append("<li>").append(product.getName())
                        .append(" (").append(product.getCategory().getDescription()).append("): ")
                        .append(product.getPrice()).append(" ₽</li>");
            }
            messageText.append("</ul>");

            boolean hasElectronicProducts = order.getProducts().stream().anyMatch(Product::hasElectronicFile);
            if (hasElectronicProducts) {
                messageText.append("<p>Ваши электронные товары (если есть) прикреплены к этому письму.</p>");
            } else {
                messageText.append("<p>В вашем заказе нет электронных товаров для прикрепления.</p>");
            }

            messageText.append("<p>С уважением,<br>Команда Книжной Гавани</p>");
            messageText.append("</body></html>");
            helper.setText(messageText.toString(), true);

            for (Product product : order.getProducts()) {
                if (product.hasElectronicFile()) {
                    try {
                        Resource resource = fileStorageService.loadAsResource(product.getElectronicProductFilename());
                        if (resource.exists() && resource.isReadable()) {
                            helper.addAttachment(product.getOriginalElectronicProductFilename(), resource);
                            log.info("Файл {} успешно прикреплен к HTML письму для заказа #{}",
                                    product.getOriginalElectronicProductFilename(), order.getId());
                        } else {
                            log.warn("Не удалось прочитать или найти файл {} для прикрепления к HTML заказу #{}",
                                    product.getElectronicProductFilename(), order.getId());
                        }
                    } catch (Exception e) {
                        log.error("Ошибка при прикреплении файла {} к HTML письму для заказа #{}: {}",
                                product.getOriginalElectronicProductFilename(), order.getId(), e.getMessage());
                    }
                }
            }
            emailSender.send(message);
            log.info("HTML подтверждение заказа #{} с вложениями успешно отправлено на email {}.", order.getId(),
                    order.getUser().getEmail());
        } catch (Exception e) {
            log.error(
                    "Ошибка отправки HTML письма с подтверждением заказа #{} и вложениями: {}. Попытка отправки упрощенного письма.",
                    order.getId(), e.getMessage(), e);
            sendSimpleOrderConfirmation(order);
        }
    }

    public void sendSimpleOrderConfirmation(Order order) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom("spring.project.42@mail.ru");
            mailMessage.setTo(order.getUser().getEmail());
            mailMessage.setSubject("Упрощенное подтверждение заказа #" + order.getId());

            StringBuilder text = new StringBuilder();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            text.append("Уважаемый(ая) ").append(order.getUser().getFirstName()).append(",\n\n");
            text.append("Благодарим вас за заказ в Книжной Гавани!\n");
            text.append("Номер вашего заказа: #").append(order.getId()).append(".\n");
            text.append("Дата заказа: ").append(order.getCreatedWhen().format(formatter)).append(".\n");
            text.append("Общая сумма: ").append(order.getTotal()).append(" ₽.\n\n");

            text.append("Состав заказа:\n");
            for (Product product : order.getProducts()) {
                text.append("- ").append(product.getName())
                        .append(" (").append(product.getCategory().getDescription()).append(")\n");
            }

            boolean hasElectronicProducts = order.getProducts().stream().anyMatch(Product::hasElectronicFile);
            if (hasElectronicProducts) {
                text.append("\nПримечание: Возникла проблема с отправкой полного подтверждения с файлами. ");
                text.append(
                        "Ваши электронные товары будут доступны в вашем личном кабинете или будут отправлены следующим письмом. ");
                text.append(
                        "Если вы не получили их или не можете получить доступ, пожалуйста, свяжитесь с нашей службой поддержки.\n");
            }

            text.append("\nС уважением,\nКоманда Книжной Гавани");

            mailMessage.setText(text.toString());
            emailSender.send(mailMessage);
            log.info("Упрощенное подтверждение заказа #{} (fallback) успешно отправлено на email {}.", order.getId(),
                    order.getUser().getEmail());
        } catch (Exception mailEx) {
            log.error(
                    "Критическая ошибка: не удалось отправить даже упрощенное подтверждение заказа #{} на email {}: {}",
                    order.getId(), order.getUser().getEmail(), mailEx.getMessage(), mailEx);
        }
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long id, String statusStr) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Заказ не найден"));

        OrderStatus status;
        try {
            status = OrderStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Недопустимый статус заказа: " + statusStr +
                    ". Допустимые значения: "
                    + String.join(", ", List.of(OrderStatus.values()).stream().map(Enum::name).toArray(String[]::new)));
        }

        order.setStatus(status);
        order.setUpdatedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        order.setUpdatedWhen(LocalDateTime.now());

        return orderMapper.toDTO(orderRepository.save(order));
    }

    protected NotFoundException createNotFoundException(Long id) {
        return new NotFoundException(Errors.Orders.ORDER_NOT_FOUND_ERROR.formatted(id));
    }
}
