package com.almetpt.coursework.bookclub.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO extends GenericDTO {
    private Long id;
    private Long userId;
    private List<ProductDTO> products;
}
