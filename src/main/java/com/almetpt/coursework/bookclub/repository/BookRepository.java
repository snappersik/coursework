package com.almetpt.coursework.bookclub.repository;

import com.almetpt.coursework.bookclub.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends GenericRepository<Book> {
    @Query(nativeQuery = true, value = """
    SELECT * FROM books b
    WHERE (LOWER(b.title) LIKE LOWER(CONCAT('%',:title,'%')) OR :title IS NULL)
    AND (LOWER(b.author) LIKE LOWER(CONCAT('%',:author,'%')) OR :author IS NULL)
    AND (LOWER(b.genre) LIKE LOWER(CONCAT('%',:genre,'%')) OR :genre IS NULL)
    AND b.is_deleted = false
""")

    Page<Book> findAllByTitleAndAuthorAndGenre(@Param("title") String title,
                                               @Param("author") String author,
                                               @Param("genre") String genre,
                                               Pageable pageable);
}

