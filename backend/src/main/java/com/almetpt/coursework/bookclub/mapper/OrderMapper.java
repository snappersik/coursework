package com.almetpt.coursework.bookclub.mapper;

import com.almetpt.coursework.bookclub.dto.OrderDTO;
import com.almetpt.coursework.bookclub.dto.ProductDTO; // Убедитесь, что ProductDTO импортирован
import com.almetpt.coursework.bookclub.model.Order;
import com.almetpt.coursework.bookclub.model.OrderStatus;
import com.almetpt.coursework.bookclub.model.Product;
import com.almetpt.coursework.bookclub.model.User;
import com.almetpt.coursework.bookclub.repository.ProductRepository;
import com.almetpt.coursework.bookclub.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.webjars.NotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OrderMapper extends GenericMapper<Order, OrderDTO> {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper; // Добавляем ProductMapper для корректного маппинга продуктов

    public OrderMapper(ModelMapper mapper, UserRepository userRepository, ProductRepository productRepository, ProductMapper productMapper) {
        super(Order.class, OrderDTO.class, mapper);
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.productMapper = productMapper; // Инициализируем
    }

    @PostConstruct
    @Override
    protected void setupMapper() {
        modelMapper.createTypeMap(Order.class, OrderDTO.class)
                .addMappings(mapper -> {
                    mapper.using(ctx -> {
                        Order source = (Order) ctx.getSource();
                        return source.getUser() != null ? source.getUser().getId() : null;
                    }).map(src -> src, OrderDTO::setUserId);
                    
                    mapper.using(ctx -> { // Маппинг userEmail
                        Order source = (Order) ctx.getSource();
                        return source.getUser() != null ? source.getUser().getEmail() : null;
                    }).map(src -> src, OrderDTO::setUserEmail);

                    mapper.using(ctx -> {
                        Order source = (Order) ctx.getSource();
                        return source.getStatus() != null ? source.getStatus().name() : null;
                    }).map(src -> src, OrderDTO::setOrderStatus);
                })
                .setPostConverter(context -> {
                    Order source = context.getSource();
                    OrderDTO destination = context.getDestination();

                    // GenericDTO уже должен смапить id, createdWhen и т.д.
                    // if (destination.getId() == null && source.getId() != null) {
                    //     destination.setId(source.getId());
                    // }
                    // if (destination.getCreatedWhen() == null && source.getCreatedWhen() != null) {
                    //     destination.setCreatedWhen(source.getCreatedWhen());
                    // }

                    if (destination.getTotal() == null && source.getTotal() != null) {
                        destination.setTotal(source.getTotal());
                    }

                    if (source.getProducts() != null) {
                        // Используем ProductMapper для маппинга списка продуктов
                        List<ProductDTO> productDTOs = source.getProducts().stream()
                                .map(productMapper::toDTO) // Используем productMapper
                                .collect(Collectors.toList());
                        destination.setProducts(productDTOs);
                    } else {
                        destination.setProducts(Collections.emptyList());
                    }

                    return destination;
                });

        modelMapper.createTypeMap(OrderDTO.class, Order.class)
                .addMappings(mapper -> {
                    mapper.skip(Order::setUser); // Будет установлено в mapSpecificFields
                    mapper.skip(Order::setStatus); // Будет установлено в mapSpecificFields
                    mapper.skip(Order::setProducts); // Будет установлено в mapSpecificFields
                })
                .setPostConverter(toEntityConverter()); // Общий конвертер из GenericMapper
    }

    @Override
    protected void mapSpecificFields(OrderDTO source, Order destination) { // DTO -> Entity
        if (source.getUserId() != null) {
            User user = userRepository.findById(source.getUserId())
                    .orElseThrow(() -> new NotFoundException("User not found with id: " + source.getUserId()));
            destination.setUser(user);
        } else if (source.getUserEmail() != null) { // Если userId нет, но есть email (маловероятно для создания)
             User user = userRepository.findByEmail(source.getUserEmail())
                    .orElseThrow(() -> new NotFoundException("User not found with email: " + source.getUserEmail()));
            destination.setUser(user);
        }


        if (source.getOrderStatus() != null) {
            try {
                destination.setStatus(OrderStatus.valueOf(source.getOrderStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid order status in DTO: {}", source.getOrderStatus());
                destination.setStatus(OrderStatus.PENDING); // Статус по умолчанию или обработка ошибки
            }
        }

        if (source.getProducts() != null) {
            List<Product> products = source.getProducts().stream()
                    .map(productDTO -> {
                        if (productDTO.getId() == null) {
                             throw new IllegalArgumentException("Product DTO in Order must have an ID");
                        }
                        return productRepository.findById(productDTO.getId())
                            .orElseThrow(() -> new NotFoundException("Product not found with id: " + productDTO.getId()));
                    })
                    .collect(Collectors.toList());
            destination.setProducts(products);
        } else {
            destination.setProducts(Collections.emptyList());
        }
    }

    @Override
    protected List<Long> getIds(Order entity) { // Не используется для Order обычно
        return Collections.emptyList();
    }
}
