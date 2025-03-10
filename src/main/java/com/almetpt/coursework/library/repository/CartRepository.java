package com.almetpt.coursework.library.repository;

import com.almetpt.coursework.library.model.Cart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends GenericRepository<Cart> {
    Page<Cart> findByUserId(Long userId, Pageable pageable);
}
