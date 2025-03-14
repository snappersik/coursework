package com.almetpt.coursework.BookClub.mapper;

import com.almetpt.coursework.BookClub.dto.OrderDTO;
import com.almetpt.coursework.BookClub.model.Order;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper extends GenericMapper<Order, OrderDTO> {

    protected OrderMapper(ModelMapper modelMapper) {
        super(Order.class, OrderDTO.class, modelMapper);
    }

    @Override
    protected void setupMapper() {
        modelMapper.createTypeMap(Order.class, OrderDTO.class)
                .setPostConverter(toDTOConverter());
        modelMapper.createTypeMap(OrderDTO.class, Order.class)
                .setPostConverter(toEntityConverter());
    }

    @Override
    protected void mapSpecificFields(OrderDTO source, Order destination) {
        // Дополнительный маппинг для полей Order, если необходимо
    }

    @Override
    protected void mapSpecificFields(Order source, OrderDTO destination) {
        // Дополнительный маппинг для полей OrderDTO, если необходимо
    }

    @Override
    protected List<Long> getIds(Order entity) {
        return null;
    }
}
