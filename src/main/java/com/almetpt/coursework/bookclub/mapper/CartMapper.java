package com.almetpt.coursework.bookclub.mapper;

import com.almetpt.coursework.bookclub.dto.CartDTO;
import com.almetpt.coursework.bookclub.model.Cart;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CartMapper {

    public CartDTO toDTO(Cart cart) {
        return new CartDTO(
                cart.getId(),
                cart.getUser().getId(),
                cart.getProducts().stream()
                        .map(product -> new CartDTO.ProductDTO(
                                product.getId(),
                                product.getName()
                        ))
                        .collect(Collectors.toList())
        );
    }
}
