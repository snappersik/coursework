package com.almetpt.coursework.BookClub.repository;

import com.almetpt.coursework.BookClub.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends GenericRepository<Book> {
    @Query(nativeQuery = true, value = """
            select * 
            from books b
            where (lower(b.title) LIKE lower(concat('%',:title,'%')) or :title is null)
              and (lower(b.author) LIKE lower(concat('%',:author,'%')) or :author is null)
              and (lower(b.genre) LIKE lower(concat('%',:genre,'%')) or :genre is null)
              and b.is_deleted = false
            """)
    Page<Book> findAllByTitleAndAuthorAndGenre(@Param("title") String title,
                                               @Param("author") String author,
                                               @Param("genre") String genre,
                                               Pageable pageable);
}

