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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.webjars.NotFoundException;

@Service
public class OrderService extends GenericService<Order, OrderDTO> {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartService cartService;
    private final JavaMailSender emailSender;

    public OrderService(OrderRepository orderRepository,
            OrderMapper orderMapper,
            UserRepository userRepository,
            CartRepository cartRepository,
            CartService cartService,
            JavaMailSender emailSender) {
        super(orderRepository, orderMapper);
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.cartService = cartService;
        this.emailSender = emailSender;
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

    @Transactional
    public OrderDTO createOrderFromCart() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findUserByEmailAndIsDeletedFalse(email);
        if (currentUser == null) {
            throw new NotFoundException("Текущий пользователь не найден");
        }

        Cart cart = cartService.getCartByUserId(currentUser.getId());
        if (cart == null) {
            throw new IllegalStateException("Корзина не найдена");
        }

        if (cart.getProducts() == null || cart.getProducts().isEmpty()) {
            throw new IllegalStateException("Корзина пуста");
        }

        Order order = new Order();
        order.setUser(currentUser);
        order.setProducts(new ArrayList<>(cart.getProducts()));
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedBy(email);
        order.setCreatedWhen(LocalDateTime.now());

        // Расчет общей суммы заказа
        BigDecimal total = cart.getProducts().stream()
                .map(product -> product.getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotal(total);

        // Сохраняем заказ
        Order savedOrder = orderRepository.save(order);

        // Отправляем письмо с подтверждением заказа
        sendOrderConfirmationEmail(savedOrder);

        // Очищаем корзину после создания заказа
        cart.getProducts().clear();
        cartRepository.save(cart);

        return orderMapper.toDTO(savedOrder);
    }

    private void sendOrderConfirmationEmail(Order order) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("spring.project.42@mail.ru");
            message.setTo(order.getUser().getEmail());
            message.setSubject("Подтверждение заказа #" + order.getId());

            StringBuilder messageText = new StringBuilder();
            messageText.append("Уважаемый(ая) ").append(order.getUser().getFirstName()).append(",\n\n");
            messageText.append("Благодарим вас за заказ в Книжной Гавани!\n\n");
            messageText.append("Номер заказа: #").append(order.getId()).append("\n");
            messageText.append("Дата заказа: ").append(order.getCreatedWhen()).append("\n");
            messageText.append("Общая сумма: ").append(order.getTotal()).append(" ₽\n\n");

            messageText.append("Состав заказа:\n");
            for (Product product : order.getProducts()) {
                messageText.append("- ").append(product.getName())
                        .append(" (").append(product.getCategory().getDescription()).append("): ")
                        .append(product.getPrice()).append(" ₽\n");
            }

            messageText
                    .append("\nВаши электронные товары будут доступны в личном кабинете после обработки заказа.\n\n");
            messageText.append("С уважением,\nКоманда Книжной Гавани");

            message.setText(messageText.toString());
            emailSender.send(message);
        } catch (Exception e) {
            // Логируем ошибку, но не прерываем создание заказа
            System.err.println("Ошибка отправки письма: " + e.getMessage());
        }
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long id, String statusStr) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Заказ не найден"));

        // Преобразуем строку в enum OrderStatus
        OrderStatus status;
        try {
            status = OrderStatus.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Недопустимый статус заказа: " + statusStr +
                    ". Допустимые значения: PENDING, COMPLETED, CANCELLED");
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