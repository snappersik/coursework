package com.almetpt.coursework.bookclub.repository;

import com.almetpt.coursework.bookclub.model.Cart;
import com.almetpt.coursework.bookclub.model.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends GenericRepository<Cart> {
    Optional<Cart> findByUser(User user);
}
