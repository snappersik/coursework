package com.almetpt.coursework.BookClub.repository;

import com.almetpt.coursework.BookClub.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends GenericRepository<Product> {
    @Query(nativeQuery = true, value = """
            select *
            from products p
            where (lower(p.product_name) LIKE lower(concat('%',:productName,'%')) or :productName is null)
              and (lower(p.category) LIKE lower(concat('%',:category,'%')) or :category is null)
              and p.is_deleted = false
            """)
    Page<Product> findAllByProductNameAndCategory(@Param("productName") String productName,
                                                  @Param("category") String category,
                                                  Pageable pageable);
}

