package com.almetpt.coursework.bookclub.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO extends GenericDTO {
    private Long id;
    private Long userId;
    private BigDecimal total;
    private String orderStatus;
    private List<ProductDTO> products;
}
