package com.almetpt.coursework.BookClub.repository;

import com.almetpt.coursework.BookClub.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends GenericRepository<Order> {
    Page<Order> findByUserId(Long userId, Pageable pageable);
}

