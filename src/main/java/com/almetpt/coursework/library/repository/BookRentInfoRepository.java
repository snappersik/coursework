package com.almetpt.coursework.library.repository;

import com.aptproject.springlibraryproject.library.model.BookRentInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRentInfoRepository extends GenericRepository<BookRentInfo>{
    Page<BookRentInfo> getBookRentInfoByUserId(Long id,
                                               Pageable pageRequest);
}
