package com.almetpt.coursework.library.repository;

import com.almetpt.coursework.library.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends GenericRepository<Order> {
    Page<Order> findByUserId(Long userId, Pageable pageable);
}
