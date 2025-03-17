package com.almetpt.coursework.BookClub.mapper;

import com.almetpt.coursework.BookClub.dto.CartDTO;
import com.almetpt.coursework.BookClub.model.Cart;
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
