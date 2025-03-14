package com.almetpt.coursework.BookClub.dto;

import com.almetpt.coursework.BookClub.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO extends GenericDTO {
    private Long id;
    private Long userId;
    private List<OrderItemDTO> orderItems;
    private OrderStatus status;
    private BigDecimal totalPrice;
}
