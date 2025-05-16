package com.almetpt.coursework.bookclub.mapper;

import com.almetpt.coursework.bookclub.dto.CartDTO;
import com.almetpt.coursework.bookclub.model.Cart;
import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import java.util.stream.Collectors;

@Component
public class CartMapper extends GenericMapper<Cart, CartDTO> {

    private final ProductMapper productMapper;

    public CartMapper(ProductMapper productMapper) {
        super(Cart.class, CartDTO.class, new ModelMapper());
        this.productMapper = productMapper;
    }

    @PostConstruct
    @Override
    protected void setupMapper() {
        // Nothing to do here, as we're handling mapping manually
    }

    @Override
    protected List<Long> getIds(Cart entity) {
        return Collections.emptyList(); // Пока идентификаторы не нужны
    }

    @Override
    public CartDTO toDTO(Cart cart) {
        return new CartDTO(
                cart.getId(),
                cart.getUser().getId(),
                cart.getProducts().stream()
                        .map(productMapper::toDTO)
                        .collect(Collectors.toList()));
    }

    @Override
    public Cart toEntity(CartDTO cartDTO) {
        Cart cart = new Cart();
        cart.setId(cartDTO.getId());
        return cart;
    }
}
