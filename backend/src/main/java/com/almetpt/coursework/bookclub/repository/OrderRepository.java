package com.almetpt.coursework.bookclub.repository;

import com.almetpt.coursework.bookclub.model.Order;
import com.almetpt.coursework.bookclub.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends GenericRepository<Order> {
    Page<Order> findByUser(User user, Pageable pageable);
    List<Order> findByUser(User user);
    long countByIsDeletedFalse();
}
