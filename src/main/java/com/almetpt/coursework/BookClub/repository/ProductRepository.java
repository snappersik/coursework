package com.almetpt.coursework.BookClub.repository;

import com.almetpt.coursework.BookClub.model.Product;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends GenericRepository<Product> {

    @Query(nativeQuery = true, value = """
            SELECT CASE
                   WHEN COUNT(*) = 0 THEN TRUE
                   ELSE FALSE
                   END
            FROM orders o
            WHERE o.product_id = :productId
            """)
    boolean isProductCanBeDeleted(@Param("productId") Long productId);
}
