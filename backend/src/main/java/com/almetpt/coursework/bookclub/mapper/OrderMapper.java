package com.almetpt.coursework.bookclub.mapper;

import com.almetpt.coursework.bookclub.dto.OrderDTO;
import com.almetpt.coursework.bookclub.dto.ProductDTO;
import com.almetpt.coursework.bookclub.model.Order;
import com.almetpt.coursework.bookclub.model.OrderStatus;
import com.almetpt.coursework.bookclub.model.Product;
import com.almetpt.coursework.bookclub.model.User;
import com.almetpt.coursework.bookclub.repository.ProductRepository;
import com.almetpt.coursework.bookclub.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.webjars.NotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper extends GenericMapper<Order, OrderDTO> {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderMapper(ModelMapper mapper, UserRepository userRepository, ProductRepository productRepository) {
        super(Order.class, OrderDTO.class, mapper);
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @PostConstruct
    @Override
    protected void setupMapper() {
        modelMapper.createTypeMap(Order.class, OrderDTO.class)
            .addMappings(mapper -> {
                // Безопасное маппирование userId
                mapper.using(ctx -> {
                    Order source = (Order) ctx.getSource();
                    return source.getUser() != null ? source.getUser().getId() : null;
                }).map(src -> src, OrderDTO::setUserId);
                
                // Безопасное маппирование статуса
                mapper.using(ctx -> {
                    Order source = (Order) ctx.getSource();
                    return source.getStatus() != null ? source.getStatus().name() : null;
                }).map(src -> src, OrderDTO::setOrderStatus);
            })
            .setPostConverter(context -> {
                Order source = context.getSource();
                OrderDTO destination = context.getDestination();
                
                // Маппинг продуктов
                if (source.getProducts() != null) {
                    List<ProductDTO> productDTOs = source.getProducts().stream()
                            .map(product -> modelMapper.map(product, ProductDTO.class))
                            .collect(Collectors.toList());
                    destination.setProducts(productDTOs);
                }
                
                return destination;
            });

        modelMapper.createTypeMap(OrderDTO.class, Order.class)
            .addMappings(mapper -> {
                mapper.skip(Order::setUser);
                mapper.skip(Order::setStatus);
                mapper.skip(Order::setProducts);
            })
            .setPostConverter(toEntityConverter());
    }

    @Override
    protected void mapSpecificFields(OrderDTO source, Order destination) {
        if (source.getUserId() != null) {
            User user = userRepository.findById(source.getUserId())
                    .orElseThrow(() -> new NotFoundException("User not found with id: " + source.getUserId()));
            destination.setUser(user);
        }

        if (source.getOrderStatus() != null) {
            destination.setStatus(OrderStatus.valueOf(source.getOrderStatus()));
        }

        if (source.getProducts() != null) {
            List<Product> products = source.getProducts().stream()
                    .map(productDTO -> productRepository.findById(productDTO.getId())
                            .orElseThrow(() -> new NotFoundException("Product not found with id: " + productDTO.getId())))
                    .collect(Collectors.toList());
            destination.setProducts(products);
        }
    }

    @Override
    protected List<Long> getIds(Order entity) {
        return Collections.emptyList();
    }
}
