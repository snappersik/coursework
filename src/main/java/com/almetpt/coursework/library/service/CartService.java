package com.almetpt.coursework.library.service;

import com.almetpt.coursework.library.model.Order;
import com.almetpt.coursework.library.dto.CartDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CartService extends GenericService<Order, CartDTO> {
    Page<CartDTO> getCartByUserId(Long userId, Pageable pageable);
}
