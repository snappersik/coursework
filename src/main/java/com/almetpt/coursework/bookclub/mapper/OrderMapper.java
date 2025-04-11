package com.almetpt.coursework.bookclub.mapper;

import com.almetpt.coursework.bookclub.dto.OrderDTO;

import com.almetpt.coursework.bookclub.model.Order;

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
                .setPostConverter(toDTOConverter());
        modelMapper.createTypeMap(OrderDTO.class, Order.class)
                .setPostConverter(toEntityConverter());
    }

    @Override
    protected List<Long> getIds(Order entity) {
        return Collections.emptyList(); // Пока идентификаторы не нужны
    }
}
