package com.almetpt.coursework.library.repository;

import com.almetpt.coursework.library.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends GenericRepository<Product> {

    @Query(nativeQuery = true, value = """
        select distinct b.*
        from books b
                left join books_authors ba on b.id = ba.book_id
                left join authors a on a.id = ba.author_id
        where b.title ilike '%' || coalesce(:title, '%') || '%'
            and cast(b.category as char) like coalesce(:category, '%')
            and coalesce(a.name, '%') ilike '%' || coalesce(:name, '%') || '%'
            and b.is_deleted = false
    """)
    Page<Product> searchBooks(
            @Param("title") String bookTitle,
            @Param("category") String category,
            @Param("name") String authorName,
            Pageable pageRequest
    );

    @Query("""
        select case when count(b) > 0 then false else true end
        from Book b join BookRentInfo bri on b.id = bri.book.id
        where b.id = :id and bri.returned = false
    """)
    boolean isBookCanBeDeleted(final Long id);
}



