package com.almetpt.coursework.bookclub.repository;

import com.almetpt.coursework.bookclub.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends GenericRepository<Order> {
    Page<Order> findByUserId(Long userId, Pageable pageable);
}

