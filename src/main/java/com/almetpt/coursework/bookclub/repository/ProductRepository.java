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
            SELECT p FROM Product p WHERE p.productName LIKE %:productName% AND p.category.name = :categoryName
            """)
    boolean isProductCanBeDeleted(@Param("productId") Long productId);

    Page<Product> findAllByProductNameAndCategory(@Param("productName") String productName,
                                                  @Param("categoryName") String categoryName,
                                                  Pageable pageable);
}
