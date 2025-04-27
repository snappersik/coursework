package com.almetpt.coursework.bookclub.repository;

import com.almetpt.coursework.bookclub.model.Product;
import com.almetpt.coursework.bookclub.model.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends GenericRepository<Product> {

    @Query(
            value = """
        SELECT CASE WHEN COUNT(*) = 0 THEN true ELSE false END
          FROM orders_products op
         WHERE op.product_id = :productId
        """,
            nativeQuery = true
    )
    boolean isProductCanBeDeleted(@Param("productId") Long productId);


    @Query("""
      SELECT p
        FROM Product p
       WHERE p.isDeleted = false
         AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
         AND (:category IS NULL OR p.category = :category)
      """
    )
    Page<Product> findAllByNameAndCategory(
            @Param("name") String name,
            @Param("category") ProductCategory category,
            Pageable pageable
    );

    Page<Product> findAllByIsDeletedFalse(Pageable pageable);
}
