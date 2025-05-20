package com.almetpt.coursework.bookclub.repository;

import com.almetpt.coursework.bookclub.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends GenericRepository<Book> {
    @Query(nativeQuery = true, value = """
            SELECT * FROM books b
            WHERE (LOWER(b.title) LIKE LOWER(CONCAT('%',:title,'%')) OR :title IS NULL)
            AND (LOWER(b.author) LIKE LOWER(CONCAT('%',:author,'%')) OR :author IS NULL)
            AND (LOWER(b.genre) LIKE LOWER(CONCAT('%',:genre,'%')) OR :genre IS NULL)
            AND b.is_deleted = false
            """)
    Page<Book> findAllByTitleAndAuthorAndGenre(
            @Param("title") String title,
            @Param("author") String author,
            @Param("genre") String genre,
            Pageable pageable);

    @Query("""
            select case when count(e) > 0 then false else true end
            from Event e
            where e.book.id = :id and e.isCancelled = false and e.date > CURRENT_TIMESTAMP
            """)
    boolean isBookCanBeDeleted(@Param("id") Long id);

    // Найти книги с reading=true - исправленная версия с Pageable
    List<Book> findByReadingTrueOrderByCreatedWhenDesc(Pageable pageable);

    // Найти последние добавленные книги - исправленная версия с Pageable
    List<Book> findByOrderByCreatedWhenDesc(Pageable pageable);

    long countByIsDeletedFalse();

}
