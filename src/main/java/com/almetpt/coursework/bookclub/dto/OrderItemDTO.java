package com.almetpt.coursework.bookclub.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO extends GenericDTO {
    private Long id;
    private Long orderId;
    private ProductDTO product;
}
