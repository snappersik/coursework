package com.almetpt.coursework.bookclub.mapper;

import com.almetpt.coursework.bookclub.dto.OrderDTO;

import com.almetpt.coursework.bookclub.model.Order;
import com.almetpt.coursework.bookclub.model.OrderStatus;

import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class OrderMapper extends GenericMapper<Order, OrderDTO> {

    public OrderMapper(ModelMapper modelMapper) {
        super(Order.class, OrderDTO.class, modelMapper);
    }

    @PostConstruct
    @Override
    protected void setupMapper() {
        modelMapper.createTypeMap(Order.class, OrderDTO.class)
            .addMappings(mapping -> {
                mapping.map(src -> src.getTotal(), OrderDTO::setTotal);
                mapping.map(src -> src.getStatus() != null ? src.getStatus().name() : null, OrderDTO::setOrderStatus);
            })
            .setPostConverter(toDTOConverter());

        modelMapper.createTypeMap(OrderDTO.class, Order.class)
            .addMappings(mapping -> {
                mapping.map(OrderDTO::getTotal, Order::setTotal);
                mapping.map(src -> src.getOrderStatus() != null ? OrderStatus.valueOf(src.getOrderStatus()) : null, Order::setStatus);
            })
            .setPostConverter(toEntityConverter());
    }

    @Override
    protected List<Long> getIds(Order entity) {
        return Collections.emptyList();
    }
}
