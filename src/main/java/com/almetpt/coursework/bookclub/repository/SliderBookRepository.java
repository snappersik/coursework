package com.almetpt.coursework.bookclub.repository;

import com.almetpt.coursework.bookclub.model.SliderBook;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Repository
public interface SliderBookRepository extends GenericRepository<SliderBook> {
    
    // Получить все книги для слайдера, отсортированные по позиции
    List<SliderBook> findAllByOrderByPositionAsc();
    
    // Найти максимальную позицию
    @Query("SELECT MAX(s.position) FROM SliderBook s")
    Integer findMaxPosition();
    
    // Проверить существует ли книга в слайдере
    boolean existsByBookId(Long bookId);
}
