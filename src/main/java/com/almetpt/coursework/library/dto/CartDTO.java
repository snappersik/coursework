package com.almetpt.coursework.library.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    private Long id; // Идентификатор корзины
    private Long userId; // Идентификатор пользователя
    private Map<Long, Integer> items; // Карта товаров и их количества (productId -> quantity)
    private Double totalPrice; // Итоговая стоимость корзины
}
