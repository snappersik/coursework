package com.almetpt.coursework.bookclub.repository;

import com.almetpt.coursework.bookclub.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends GenericRepository<Product> {

    @Query(nativeQuery = true, value = """
    SELECT CASE WHEN COUNT(*) > 0 THEN false ELSE true END
    FROM orders_products op
    WHERE op.product_id = :productId
    """)

    boolean isProductCanBeDeleted(@Param("productId") Long productId);


    Page<Product> findAllByNameAndCategory(@Param("productName") String name,
                                           @Param("categoryName") String category,
                                           Pageable pageable);

}
