package com.almetpt.coursework.bookclub.service;

import com.almetpt.coursework.bookclub.constants.Errors;
import com.almetpt.coursework.bookclub.dto.OrderDTO;
import com.almetpt.coursework.bookclub.mapper.OrderMapper;
import com.almetpt.coursework.bookclub.model.Cart;
import com.almetpt.coursework.bookclub.model.Order;
import com.almetpt.coursework.bookclub.model.OrderStatus;
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

    public OrderService(OrderRepository orderRepository,
            OrderMapper orderMapper,
            UserRepository userRepository,
            CartRepository cartRepository,
            CartService cartService) {
        super(orderRepository, orderMapper);
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.cartService = cartService;
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
        System.out.println("Текущий email пользователя: " + email);

        User currentUser = userRepository.findUserByEmailAndIsDeletedFalse(email);
        if (currentUser == null) {
            System.out.println("Пользователь не найден по email: " + email);
            throw new NotFoundException("Текущий пользователь не найден");
        }

        System.out.println("Найден пользователь с ID: " + currentUser.getId());

        Cart cart = cartService.getCartByUserId(currentUser.getId());
        if (cart == null) {
            System.out.println("Корзина не найдена для пользователя с ID: " + currentUser.getId());
            throw new IllegalStateException("Корзина не найдена");
        }

        System.out.println("Найдена корзина с ID: " + cart.getId() + ", количество товаров: " +
                (cart.getProducts() != null ? cart.getProducts().size() : 0));

        if (cart.getProducts() == null || cart.getProducts().isEmpty()) {
            System.out.println("Корзина пуста");
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

        // Очищаем корзину после создания заказа
        cart.getProducts().clear();
        cartRepository.save(cart);

        return orderMapper.toDTO(orderRepository.save(order));
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