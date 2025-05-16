package com.almetpt.coursework.bookclub.repository;

import com.almetpt.coursework.bookclub.model.Cart;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends GenericRepository<Cart> {
    Cart findByUserId(Long userId);
}
