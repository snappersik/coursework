package com.almetpt.coursework.BookClub.service;

import com.almetpt.coursework.BookClub.dto.CartDTO;
import com.almetpt.coursework.BookClub.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CartService extends GenericService<Order, CartDTO> {
    Page<CartDTO> getCartByUserId(Long userId, Pageable pageable);
}
