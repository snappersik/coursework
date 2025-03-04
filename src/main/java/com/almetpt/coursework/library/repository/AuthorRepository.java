package com.almetpt.coursework.library.repository;

import com.almetpt.coursework.library.model.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorRepository extends GenericRepository<Author> {

    Page<Author> findAllByAuthorNameContainsIgnoreCaseAndIsDeletedFalse(String fio, Pageable pageable);

    @Query(value = """
        select case when count(a) > 0 then false else true end
        from Author a join a.books b
                    join BookRentInfo bri on b.id = bri.book.id
        where a.id = :authorId
        and bri.returned = false
        """)
    boolean checkAuthorForDeletion(final Long authorId);
}
