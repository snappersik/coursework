package com.almetpt.coursework.bookclub.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
