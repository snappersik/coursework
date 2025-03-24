package com.almetpt.coursework.bookclub.mapper;

import com.almetpt.coursework.bookclub.dto.OrderItemDTO;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderItemMapper extends GenericMapper<OrderItem, OrderItemDTO> {

    protected OrderItemMapper(ModelMapper modelMapper) {
        super(OrderItem.class, OrderItemDTO.class, modelMapper);
    }

    @Override
    protected void setupMapper() {
        modelMapper.createTypeMap(OrderItem.class, OrderItemDTO.class)
                .setPostConverter(toDTOConverter());
        modelMapper.createTypeMap(OrderItemDTO.class, OrderItem.class)
                .setPostConverter(toEntityConverter());
    }

    @Override
    protected void mapSpecificFields(OrderItemDTO source, OrderItem destination) {
        // Дополнительный маппинг для полей OrderItem, если необходимо
    }

    @Override
    protected void mapSpecificFields(OrderItem source, OrderItemDTO destination) {
        // Дополнительный маппинг для полей OrderItemDTO, если необходимо
    }

    @Override
    protected List<Long> getIds(OrderItem entity) {
        return null;
    }
}
