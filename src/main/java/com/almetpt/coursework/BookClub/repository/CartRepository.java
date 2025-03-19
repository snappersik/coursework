package com.almetpt.coursework.BookClub.repository;

import com.almetpt.coursework.BookClub.model.Cart;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends GenericRepository<Cart> {
    Cart findByUserId(Long userId);
}
